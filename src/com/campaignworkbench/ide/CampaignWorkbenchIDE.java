package com.campaignworkbench.ide;

import atlantafx.base.theme.CupertinoDark;
import atlantafx.base.theme.CupertinoLight;
import com.campaignworkbench.campaignrenderer.*;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Orientation;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Builds a User Interface for the Campaign Workbench IDE
 */
public class CampaignWorkbenchIDE extends Application {

    private final String workspacePath = "Workspaces/Test Workspace";

    private ToolBar toolBar;
    private EditorTabPanel editorTabPanel;
    private LogPanel logPanel;
    private OutputPreviewPanel previewPanel;
    private SourcePreviewPanel sourcePanel;

    private File xmlContextFile;
    private String xmlContextContent;

    static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Campaign Workbench");

        // Menu and toolbar
        MainMenuBar menuBar = new MainMenuBar(_ -> openFile(getWorkspaceTemplatePath()),
                _ -> openFile(getWorkspaceModulePath()),
                _ -> openFile(getWorkspaceBlockPath()),
                _ -> openFile(getWorkspaceXmlPath()),
                _ -> saveCurrent(),
                _ -> applyTheme(IDETheme.LIGHT),
                _ -> applyTheme(IDETheme.DARK)
        );

        toolBar = new ToolBar(_ -> openFile(getWorkspaceTemplatePath()),
                _ -> setXmlContext(),
                _ -> clearXmlContext(),
                _ -> runTemplate());

        // Editor tabs
        editorTabPanel = new EditorTabPanel((_, _, newTab) -> updateRunButtonState(newTab));

        // Output panes
        previewPanel = new OutputPreviewPanel();
        sourcePanel = new SourcePreviewPanel();

        // Log pane
        logPanel = new LogPanel();

        // Create the right hand side preview split panes
        SplitPane previewSplitPane = new SplitPane();
        previewSplitPane.setOrientation(Orientation.VERTICAL);
        previewSplitPane.getItems().addAll(previewPanel.getNode(), sourcePanel.getNode());
        previewSplitPane.setDividerPositions(0.7);

        // --- Right pane (holds preview split) ---
        BorderPane rightPane = new BorderPane();
        rightPane.setCenter(previewSplitPane);

        // Main horizontal split (editor tabs | preview)
        SplitPane mainSplitPane = new SplitPane();
        mainSplitPane.setOrientation(Orientation.HORIZONTAL);
        mainSplitPane.getItems().addAll(editorTabPanel.getNode(), rightPane);
        mainSplitPane.setDividerPositions(0.5);

        VBox topBar = new VBox(menuBar.getNode(), toolBar.getNode());

        SplitPane rootSplitPane = new SplitPane();
        rootSplitPane.setOrientation(Orientation.VERTICAL);
        rootSplitPane.getItems().addAll(mainSplitPane, logPanel.getNode()); // logBox = VBox with logLabel + logArea
        rootSplitPane.setDividerPositions(0.8); // 80% main area, 20% log initially

        // --- Root BorderPane ---
        BorderPane root = new BorderPane();
        root.setTop(topBar);
        root.setCenter(rootSplitPane);
        Scene scene = new Scene(root, 1200, 800);
        primaryStage.setScene(scene);
        primaryStage.show();

        ThemeManager.applyCurrentTheme();

        appendLog("Welcome to Campaign workbench! Load a template, select an XML context, and hit run!");
    }

    @Override
    public void  stop() {
        Platform.exit();
        System.exit(0);
    }

    private void appendLog(String logMessage) {
        logPanel.appendLog(logMessage);
    }

    private String getWorkspaceTemplatePath() {
        return workspacePath + "/Templates";
    }

    private String getWorkspaceBlockPath() {
        return workspacePath + "/PersoBlocks";
    }

    private String getWorkspaceXmlPath() {
        return workspacePath + "/XmlContext";
    }

    private String getWorkspaceModulePath() {return workspacePath + "/Modules";}

    private void applyTheme(IDETheme theme) {
        ThemeManager.setTheme(theme);
    }

    /**
     * Sets the IDE theme
     * @param ideTheme theme, LIGHT or DARK, to apply to the IDE
     */
    public static void setTheme(IDETheme ideTheme) {
        switch(ideTheme)
        {
            case DARK:
                Application.setUserAgentStylesheet(new CupertinoDark().getUserAgentStylesheet());
                break;

            case LIGHT:
            default:
                Application.setUserAgentStylesheet(new CupertinoLight().getUserAgentStylesheet());
                break;
        }
    }

    private void updateRunButtonState(Tab tab) {
        if (tab instanceof EditorTab editorTab) {
            String name = editorTab.getFile()
                    .getFileName()
                    .toString()
                    .toLowerCase();
            toolBar.setRunButtonState(!name.endsWith(".template"));
        } else {
            toolBar.setRunButtonState(true);
        }
    }

    private void setXmlContext() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open File");
        fileChooser.setInitialDirectory(
                new File(System.getProperty("user.dir"), workspacePath + "/XmlContext")
        );
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("All Files", "*.*"),
                new FileChooser.ExtensionFilter("XML Files", "*.xml")
        );

        File selectedFile = fileChooser.showOpenDialog(editorTabPanel.getWindow());

        if (selectedFile != null && selectedFile.getName().endsWith(".xml")) {
            xmlContextFile = selectedFile;
            toolBar.setXmlContextLabel(selectedFile.getName());
            try {
                xmlContextContent = Files.readString(xmlContextFile.toPath());
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        }
    }

    private void clearXmlContext() {
        xmlContextFile = null;
        xmlContextContent = "";
        toolBar.clearXmlContextLabel();
    }

    private void openFile(String defaultSubfolder) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open File");
        fileChooser.setInitialDirectory(
                new File(System.getProperty("user.dir"), defaultSubfolder)
        );

        File selectedFile = fileChooser.showOpenDialog(editorTabPanel.getWindow());
        if (selectedFile == null) return;

        try {
            Path path = selectedFile.toPath();
            String content = Files.readString(path);

            editorTabPanel.addEditorTab(path, content);

            EditorTab tab = new EditorTab(path, content);
            tab.setClosable(true);

        } catch (IOException ex) {
            ex.printStackTrace();
            showAlert("Failed to open file: " + ex.getMessage());
        }
    }

    private void saveCurrent() {

        if (!editorTabPanel.isSelected()) {
            appendLog("No editor tab selected to save.");
            showAlert("No editor tab selected.");
            return;
        }

        Path file = editorTabPanel.getSelectedFile();
        String content = editorTabPanel.getSelectedText();

        try {
            Files.writeString(file, content);
            appendLog("Saved file: " + editorTabPanel.getSelectedFileName());
        } catch (IOException e) {
            appendLog("Failed to save file: " + editorTabPanel.getSelectedFileName());
            appendLog(e.getMessage());
            showAlert("Failed to save file: " + e.getMessage());
        }
    }

    private void runTemplate() {
        if (xmlContextFile == null) {
            showAlert("No XML context file set!");
            appendLog("No XML context file set!");
            return;
        }

        if (!editorTabPanel.isSelected()) {
            showAlert("No template tab selected!");
            appendLog("No template tab selected!");
            return;
        }

        try {
            String templateSource = editorTabPanel.getSelectedText();

            Context cx = Context.enter();
            cx.setOptimizationLevel(-1);

            Scriptable scope = cx.initStandardObjects();

            cx.evaluateString(
                    scope,
                    "var rtEvent = new XML(`" + xmlContextContent + "`);",
                    xmlContextFile.getName(),
                    1,
                    null
            );

            scope.put("xmlContext", scope, xmlContextContent);

            TemplateRenderResult renderResult = TemplateRenderer.render(
                    templateSource,
                    cx,
                    scope,
                    editorTabPanel.getSelectedFileName()
            );

            String resultHtml = renderResult.renderedOutput();
            String resultJs = renderResult.generatedJavaScript();

            Platform.runLater(() -> {
                previewPanel.setContent(resultHtml);
                sourcePanel.setText(resultJs);

                appendLog("Template ran successfully: " + editorTabPanel.getSelectedFileName());
            });
        } catch (TemplateParseException parseEx) {
            appendLog("An error occurred parsing the template: " + parseEx.getTemplateName() + " at line: " + parseEx.getTemplateLine());
            appendLog(parseEx.getMessage());
            Throwable cause = parseEx.getCause();
            while (cause != null) {
                appendLog("Caused by: " + cause.getClass().getSimpleName() + " : " + cause.getMessage());
                cause = cause.getCause();
            }
            appendLog(parseEx.getSourceCode());
        } catch (TemplateExecutionException execEx) {
            appendLog("An error occurred executing the template: " + execEx.getTemplateName() + " at line: " + execEx.getTemplateLine());
            appendLog(execEx.getMessage());
            Throwable cause = execEx.getCause();
            while (cause != null) {
                appendLog("Caused by: " + cause.getClass().getSimpleName() + " : " + cause.getMessage());
                cause = cause.getCause();
            }
            appendLog(execEx.getSourceCode());
        } catch (TemplateGenerationException genEx) {

        }
        catch (Exception ex) {
            ex.printStackTrace();
            showAlert("Error running template: " + ex.getMessage());
            appendLog("Error running template: " + ex.getMessage());
        } finally {
            Context.exit();
        }
    }

    private void showAlert(String msg) {
        Platform.runLater(() -> {
            Alert alert = new Alert(
                    Alert.AlertType.WARNING,
                    msg,
                    ButtonType.OK
            );
            alert.showAndWait();
        });
    }


}
