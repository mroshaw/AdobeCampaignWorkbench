package com.campaignworkbench.ide;

import atlantafx.base.theme.CupertinoDark;
import atlantafx.base.theme.CupertinoLight;
import com.campaignworkbench.campaignrenderer.*;
import com.campaignworkbench.ide.editor.SyntaxType;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Orientation;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
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

    private final String defaultWorkspacePath = "Workspaces/Test Workspace";
    private Workspace currentWorkspace;
    private WorkspaceExplorer workspaceExplorer;
    private ToolBar toolBar;
    private EditorTabPanel editorTabPanel;
    private LogPanel logPanel;
    private ErrorLogPanel errorLogPanel;
    private OutputPreviewPanel previewPanel;
    private SourcePreviewPanel postSourcePanel;
    private SourcePreviewPanel preSourcePanel;

    private File xmlContextFile;
    private String xmlContextContent;

    static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Campaign Workbench");

        // Menu and toolbar
        MainMenuBar menuBar = new MainMenuBar( event -> openWorkspace(),
                event -> openFile(getWorkspaceTemplatePath()),
                event -> openFile(getWorkspaceModulePath()),
                event -> openFile(getWorkspaceBlockPath()),
                event -> openFile(getWorkspaceXmlPath()),
                event -> saveCurrent(),
                event -> applyTheme(IDETheme.LIGHT),
                event -> applyTheme(IDETheme.DARK)
        );

        toolBar = new ToolBar(event -> openWorkspace(),
                event -> setContextXml(),
                event -> clearXmlContext(),
                event -> runTemplate());

        // Workspace Explorer
        workspaceExplorer = new WorkspaceExplorer("Workspace Explorer", this::openFileFromWorkspace);

        // Editor tabs
        editorTabPanel = new EditorTabPanel((obs, oldTab, newTab) -> updateRunButtonState(newTab));

        // Output panes
        previewPanel = new OutputPreviewPanel("Preview WebView");
        preSourcePanel = new SourcePreviewPanel("Pre Process JavaScript", SyntaxType.SOURCE_PREVIEW);
        postSourcePanel = new SourcePreviewPanel("Post Process HTML", SyntaxType.HTML_PREVIEW);

        // Log pane
        logPanel = new LogPanel("Logs");
        errorLogPanel = new ErrorLogPanel("Errors");
        errorLogPanel.setOnErrorDoubleClicked((fileName, line) -> {
            // Find the file in the workspace
            Path filePath = findFileInWorkspace(fileName);
            if (filePath != null) {
                if (editorTabPanel.isOpened(filePath)) {
                    editorTabPanel.openFileAndGoToLine(filePath, line);
                } else {
                    openFileInNewTab(filePath.toFile());
                    // Wait a bit for the tab to be created and editor to be ready
                    Platform.runLater(() -> editorTabPanel.openFileAndGoToLine(filePath, line));
                }
            }
        });
        SplitPane logSplitPane = new SplitPane();
        logSplitPane.setOrientation(Orientation.HORIZONTAL);
        logSplitPane.getItems().addAll(logPanel.getNode(), errorLogPanel.getNode());
        logSplitPane.setDividerPositions(0.5);

        // Create the right hand side preview split panes
        SplitPane previewSplitPane = new SplitPane();
        previewSplitPane.setOrientation(Orientation.VERTICAL);
        previewSplitPane.getItems().addAll(previewPanel.getNode(), postSourcePanel.getNode(), preSourcePanel.getNode());
        previewSplitPane.setDividerPositions(0.5, 0.75);
        // Workspace explorer (left-most pane)
        // --- Split: Workspace Explorer | Editor Tabs ---
        SplitPane workspaceEditorSplit = new SplitPane();
        workspaceEditorSplit.setOrientation(Orientation.HORIZONTAL);
        workspaceEditorSplit.getItems().addAll(
                workspaceExplorer.getNode(),
                editorTabPanel.getNode()
        );
        workspaceEditorSplit.setDividerPositions(0.3);

        // --- Right pane (holds preview split) ---
        BorderPane rightPane = new BorderPane();
        rightPane.setCenter(previewSplitPane);

        // --- Split: (Workspace+Editor) | Preview ---
        SplitPane mainSplitPane = new SplitPane();
        mainSplitPane.setOrientation(Orientation.HORIZONTAL);
        mainSplitPane.getItems().addAll(
                workspaceEditorSplit,
                rightPane
        );
        mainSplitPane.setDividerPositions(0.6);


        VBox topBar = new VBox(menuBar.getNode(), toolBar.getNode());

        SplitPane rootSplitPane = new SplitPane();
        rootSplitPane.setOrientation(Orientation.VERTICAL);
        rootSplitPane.getItems().addAll(mainSplitPane, logSplitPane); // logBox = VBox with logLabel + logArea
        rootSplitPane.setDividerPositions(0.8); // 80% main area, 20% log initially

        // --- Root BorderPane ---
        BorderPane root = new BorderPane();
        root.setTop(topBar);
        root.setCenter(rootSplitPane);
        Scene scene = new Scene(root, 1200, 800);
        primaryStage.setScene(scene);
        primaryStage.show();

        toolBar.setContextXmlState(false);

        ThemeManager.applyCurrentTheme();

        appendLog("Welcome to Campaign workbench! Open a workspace, load a template, select an XML context, and hit run!");
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
        return currentWorkspace.getTemplatesPath().toString();
    }

    private String getWorkspaceBlockPath() {
        return currentWorkspace.getBlocksPath().toString();
    }

    private String getWorkspaceXmlPath() {
        return currentWorkspace.getContextXmlPath().toString();
    }

    private String getWorkspaceModulePath() {
        return currentWorkspace.getModulesPath().toString();
    }

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
        if (tab instanceof EditorTab) {
            EditorTab editorTab = (EditorTab) tab;
            String name = editorTab.getFile()
                    .getFileName()
                    .toString()
                    .toLowerCase();
            toolBar.setRunButtonState(name.endsWith(".template"));
        } else {
            toolBar.setRunButtonState(true);
        }
    }

    private void openWorkspace() {

        DirectoryChooser chooser = new DirectoryChooser();
        File initialDir = new File(System.getProperty("user.dir"), "Workspaces");
        if (!initialDir.exists()) {
            initialDir.mkdirs(); // optional: create it if it doesn't exist
        }
        chooser.setInitialDirectory(initialDir);

        chooser.setTitle("Open Workspace");

        File selected = chooser.showDialog(editorTabPanel.getWindow());
        if (selected == null) return;

        Workspace ws = new Workspace(selected.toPath());
        if (!ws.isValid()) {
            showAlert("Selected folder is not a valid workspace.");
            return;
        }

        this.currentWorkspace = ws;
        workspaceExplorer.displayWorkspace(ws);

        toolBar.setContextXmlState(true);

        appendLog("Workspace opened: " + selected.getAbsolutePath());
    }


    private void openFileFromWorkspace(File file) {
            openFileInNewTab(file);
    }

    private void setContextXml() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open File");
        fileChooser.setInitialDirectory(
                new File(currentWorkspace.getContextXmlPath().toString())
        );
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("All Files", "*.*"),
                new FileChooser.ExtensionFilter("XML Files", "*.xml")
        );

        File selectedFile = fileChooser.showOpenDialog(editorTabPanel.getWindow());

        if (selectedFile != null && selectedFile.getName().endsWith(".xml")) {
            xmlContextFile = selectedFile;
            toolBar.setContextXmlLabel(selectedFile.getName());
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

        openFileInNewTab(selectedFile);
    }

    private void openFileInNewTab(File selectedFile) {
        try {
            Path path = selectedFile.toPath();
            String content = Files.readString(path);

            editorTabPanel.addEditorTab(path, content);

            // EditorTab tab = new EditorTab(path, content);
            // tab.setClosable(true);

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

        errorLogPanel.clearErrors();

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
                    currentWorkspace,
                    templateSource,
                    cx,
                    scope,
                    editorTabPanel.getSelectedFileName()
            );

            String resultHtml = renderResult.renderedOutput();
            String resultJs = renderResult.generatedJavaScript();

            Platform.runLater(() -> {
                previewPanel.setContent(resultHtml);
                postSourcePanel.setText(resultHtml);
                preSourcePanel.setText(resultJs);
                appendLog("Template ran successfully: " + editorTabPanel.getSelectedFileName());
            });
        } catch (TemplateException ex) {
            appendLog("An error occurred: " + ex.getMessage());
            errorLogPanel.addError(ex);
            preSourcePanel.setText(ex.getSourceCode());
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

    private Path findFileInWorkspace(String fileName) {
        if (currentWorkspace == null) return null;
        for (File file : currentWorkspace.getAllFiles()) {
            if (file.getName().equals(fileName)) {
                return file.toPath();
            }
        }
        return null;
    }


}
