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
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;


import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static com.campaignworkbench.campaignrenderer.Workspace.WorkspaceFileType.*;

/**
 * Builds a User Interface for the Campaign Workbench IDE
 */
public class CampaignWorkbenchIDE extends Application {

    /**
     * The current active workspace
     */
    private Workspace currentWorkspace;
    private WorkspaceExplorer workspaceExplorer;
    private ToolBar toolBar;
    private EditorTabPanel editorTabPanel;
    private LogPanel logPanel;
    private ErrorLogPanel errorLogPanel;
    private OutputPreviewPanel previewPanel;
    private SourcePreviewPanel postSourcePanel;
    private SourcePreviewPanel preSourcePanel;

    /**
     * Main entry point for the application
     *
     * @param args command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }


    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Campaign Workbench");

        // Set the icon
        Image iconImage = new Image(
                getClass().getResourceAsStream("/app.png"));
        primaryStage.getIcons().add(iconImage);

        // Menu and toolbar
        MainMenuBar menuBar = new MainMenuBar(
                event -> newWorkspace(),
                event -> openWorkspace(),
                event -> saveWorkspace(),

                event -> createNewFile(TEMPLATE),
                event -> createNewFile(Workspace.WorkspaceFileType.MODULE),
                event -> createNewFile(Workspace.WorkspaceFileType.BLOCK),
                event -> createNewFile(Workspace.WorkspaceFileType.CONTEXT),

                event -> addExistingFile(TEMPLATE),
                event -> addExistingFile(Workspace.WorkspaceFileType.MODULE),
                event -> addExistingFile(Workspace.WorkspaceFileType.BLOCK),
                event -> addExistingFile(Workspace.WorkspaceFileType.CONTEXT),

                event -> saveCurrent(),
                event -> saveCurrentAs(),

                event -> applyTheme(IDETheme.LIGHT),
                event -> applyTheme(IDETheme.DARK),

                event -> showAbout(),
                event -> exitApplication()
        );

        toolBar = new ToolBar(event -> openWorkspace(),
                event -> setContextXml(),
                event -> clearContextXml(),
                event -> runTemplate());

        // Workspace Explorer
        workspaceExplorer = new WorkspaceExplorer("Workspace Explorer", currentWorkspace, this::openFileFromWorkspace);

        // Editor tabs
        editorTabPanel = new EditorTabPanel((obs, oldTab, newTab) -> tabPanelChanged(newTab));

        // Output panes
        previewPanel = new OutputPreviewPanel("Preview WebView");
        preSourcePanel = new SourcePreviewPanel("Pre Process JavaScript", SyntaxType.SOURCE_PREVIEW);
        postSourcePanel = new SourcePreviewPanel("Post Process HTML", SyntaxType.HTML_PREVIEW);

        // Log pane
        logPanel = new LogPanel("Logs");
        errorLogPanel = new ErrorLogPanel("Errors");
        errorLogPanel.setOnErrorDoubleClicked((workspaceFile, line) -> {
            // Find the file in the workspace
            Path filePath = workspaceFile.getFilePath();
            if (filePath != null) {
                if (editorTabPanel.isOpened(filePath)) {
                    editorTabPanel.openFileAndGoToLine(filePath, line);
                } else {
                    openFileInNewTab(workspaceFile);
                    // Wait a bit for the tab to be created and editor to be ready
                    Platform.runLater(() -> editorTabPanel.openFileAndGoToLine(filePath, line));
                }
            } else {
                // If it's not a physical file, it might be the "RenderedTemplate" (the preprocessed staging source)
                // in which case we don't open a file, but we might want to log it.
                appendLog("Could not find file: " + filePath);
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
        previewSplitPane.setDividerPositions(0.33, 0.66);
        // Workspace explorer (left-most pane)

        // --- Split: Workspace Explorer | Editor Tabs ---
        SplitPane workspaceEditorSplit = new SplitPane();
        workspaceEditorSplit.setOrientation(Orientation.HORIZONTAL);
        workspaceEditorSplit.getItems().addAll(
                workspaceExplorer.getNode(),
                editorTabPanel.getNode()
        );
        workspaceEditorSplit.setDividerPositions(0.3);
        SplitPane.setResizableWithParent(workspaceExplorer.getNode(), false);

        // --- Right pane (holds preview split) ---
        BorderPane previewPane = new BorderPane();
        previewPane.setCenter(previewSplitPane);

        // --- Split: (Workspace+Editor) | Preview ---
        SplitPane editorPreviewSplit = new SplitPane();
        editorPreviewSplit.setOrientation(Orientation.HORIZONTAL);
        editorPreviewSplit.getItems().addAll(
                workspaceEditorSplit,
                previewPane
        );
        editorPreviewSplit.setDividerPositions(0.6);
        SplitPane.setResizableWithParent(previewSplitPane, false);

        VBox topBar = new VBox(menuBar.getNode(), toolBar.getNode());

        SplitPane rootSplitPane = new SplitPane();
        rootSplitPane.setOrientation(Orientation.VERTICAL);
        rootSplitPane.getItems().addAll(editorPreviewSplit, logSplitPane); // logBox = VBox with logLabel + logArea
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

    /**
     * Stop the application
     */
    @Override
    public void stop() {
        exitApplication();
    }

    private void exitApplication() {
        Platform.exit();
        System.exit(0);
    }

    /**
     * Appends a message to the log panel
     *
     * @param logMessage the message to append
     */
    private void appendLog(String logMessage) {
        logPanel.appendLog(logMessage);
    }

    /**
     * @return the path to the templates folder in the current workspace
     */
    private String getWorkspaceTemplatePath() {
        return currentWorkspace.getTemplatesPath().toString();
    }

    /**
     * @return the path to the blocks folder in the current workspace
     */
    private String getWorkspaceBlockPath() {
        return currentWorkspace.getBlocksPath().toString();
    }

    /**
     * @return the path to the context XML folder in the current workspace
     */
    private String getWorkspaceContextPath() {
        return currentWorkspace.getContextXmlPath().toString();
    }

    /**
     * @return the path to the modules folder in the current workspace
     */
    private String getWorkspaceModulePath() {
        return currentWorkspace.getModulesPath().toString();
    }

    /**
     * Applies a theme to the IDE
     *
     * @param theme the theme to apply
     */
    private void applyTheme(IDETheme theme) {
        ThemeManager.setTheme(theme);
    }

    /**
     * Sets the IDE theme
     *
     * @param ideTheme theme, LIGHT or DARK, to apply to the IDE
     */
    public static void setTheme(IDETheme ideTheme) {
        String stylesheet = switch (ideTheme) {
            case DARK -> new CupertinoDark().getUserAgentStylesheet();
            case LIGHT -> new CupertinoLight().getUserAgentStylesheet();
        };
        Application.setUserAgentStylesheet(null);
        Application.setUserAgentStylesheet(stylesheet);
    }

    /**
     * Updates the run button state based on the current tab
     *
     * @param tab the currently selected tab
     */
    private void tabPanelChanged(Tab tab) {
        if (tab instanceof EditorTab editorTab) {
            toolBar.setRunButtonState(editorTab.isTemplateTab());
            toolBar.setContextXmlState(editorTab.isContextApplicable());
            toolBar.setClearContextXmlState(editorTab.isContextApplicable());
        }
    }

    private void newWorkspace() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Workspace JSON file");
        fileChooser.setInitialDirectory(
                Workspace.getWorkspacesRootPath().toFile()
        );
        fileChooser.setInitialFileName("workspace.json");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Workbench JSON files", "*.json")
        );

        File selectedFile = fileChooser.showSaveDialog(editorTabPanel.getWindow());

        if (selectedFile == null) {
            return;
        }

        currentWorkspace = new Workspace(selectedFile.toPath(), true);
        workspaceExplorer.setWorkspace(currentWorkspace);
    }

    private void saveWorkspace() {
        if (currentWorkspace != null) {
            try {
                currentWorkspace.writeToJson();
                logPanel.appendLog("Workspace saved successfully!");
            } catch (IDEException ideException) {
                reportError("Could not save workspace!", ideException, true);
            }
        }
    }

    private void saveWorkspaceAs() {

    }

    /**
     * Opens a workspace directory
     */
    private void openWorkspace() {

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open Workspace JSON file");
        fileChooser.setInitialDirectory(
                Workspace.getWorkspacesRootPath().toFile()
        );
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Workbench JSON files", "*.json")
        );

        File selectedFile = fileChooser.showOpenDialog(editorTabPanel.getWindow());

        if (selectedFile == null) return;

        if (selectedFile.getName().endsWith(".json")) {

            currentWorkspace = new Workspace(selectedFile.toPath(), false);

            currentWorkspace.openWorkspace(selectedFile.toPath());
            workspaceExplorer.setWorkspace(currentWorkspace);
            return;
        }

        showAlert("Selected file is not a valid workspace JSON file.");
        appendLog("Workspace opened: " + selectedFile.getAbsolutePath());
    }

    /**
     * Opens a file from the workspace explorer
     *
     * @param workspaceFile the file to open
     */
    private void openFileFromWorkspace(WorkspaceFile workspaceFile) {
        openFileInNewTab(workspaceFile);
    }

    /**
     * Sets the XML context for the currently selected tab file
     */
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

            editorTabPanel.setSelectedContextFile(selectedFile.toPath());
        }
    }

    /**
     * Clears the current XML context
     */
    private void clearContextXml() {
        editorTabPanel.clearSelectedContextFile();
    }

    /**
     * Opens a file from the file system
     *
     */
    private void addExistingFile(Workspace.WorkspaceFileType fileType) {
        FileChooser fileChooser = new FileChooser();
        FileChooserConfig chooserConfig = getFileChooserConfig(fileType);

        fileChooser.setTitle(chooserConfig.title());
        fileChooser.setInitialDirectory(chooserConfig.defaultFolder());;
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter(chooserConfig.description(), chooserConfig.extension())
        );

        File selectedFile = fileChooser.showOpenDialog(editorTabPanel.getWindow());
        if (selectedFile == null) return;

        try {
            currentWorkspace.addExistingWorkspaceFile(selectedFile.toPath(), fileType);
            workspaceExplorer.refreshWorkspace();
        } catch (IDEException ideEx) {
            reportError("An error occurred while adding an existing file of type: " + fileType, ideEx, true);
        }
    }

    private void createNewFile(Workspace.WorkspaceFileType fileType) {
        FileChooser fileChooser = new FileChooser();

        FileChooserConfig chooserConfig = getFileChooserConfig(fileType);

        fileChooser.setTitle(chooserConfig.title());
        fileChooser.setInitialDirectory(chooserConfig.defaultFolder());
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter(chooserConfig.description(), chooserConfig.extension())
        );

        File selectedFile = fileChooser.showSaveDialog(editorTabPanel.getWindow());

        if (selectedFile == null) {
            return;
        }

        if(currentWorkspace != null) {
            try {
                currentWorkspace.addNewWorkspaceFile(selectedFile.toPath(), fileType);
                workspaceExplorer.refreshWorkspace();
            } catch (IDEException ideEx) {
                reportError("An error occurred while creating an new file of type: " + fileType, ideEx, true);
            }
        }
    }

    private FileChooserConfig getFileChooserConfig(Workspace.WorkspaceFileType fileType) {
        return switch (fileType) {
            case TEMPLATE -> new FileChooserConfig(
                    "Create new template file",
                    new File(getWorkspaceTemplatePath()),
                    "Template files",
                    "*.template"
            );
            case MODULE -> new FileChooserConfig(
                    "Create new module file",
                    new File(getWorkspaceModulePath()),
                    "ETM Module files",
                    "*.module"
            );
            case BLOCK -> new FileChooserConfig(
                    "Create new block file",
                    new File(getWorkspaceBlockPath()),
                    "Block files",
                    "*.block"
            );
            case CONTEXT -> new FileChooserConfig(
                    "Create new context file",
                    new File(getWorkspaceContextPath()),
                    "Context XML files",
                    "*.xml"
            );
        };
    }


    /**
     * Opens a file in a new editor tab
     *
     * @param workspaceFile the file to open
     */
    private void openFileInNewTab(WorkspaceFile workspaceFile) {
        editorTabPanel.addEditorTab(workspaceFile);
    }

    /**
     * Saves the content of the currently selected editor tab
     */
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

    private void saveCurrentAs() {

    }

    /**
     * Runs the template in the currently selected editor tab
     */
    private void runTemplate() {
        if (!editorTabPanel.isSelectedTemplateAndReady()) {
            showAlert("No XML context file set!");
            appendLog("No XML context file set!");
            return;
        }

        errorLogPanel.clearErrors();

        try {
            WorkspaceFile selectedWorkspaceFile = editorTabPanel.getSelectedWorkspaceFile();

            if(selectedWorkspaceFile instanceof WorkspaceContextFile workspaceContextFile) {

                TemplateRenderResult renderResult = TemplateRenderer.render(
                        currentWorkspace,
                        workspaceContextFile
                );

                String resultHtml = renderResult.renderedOutput();
                String resultJs = renderResult.generatedJavaScript();

                Platform.runLater(() -> {
                    previewPanel.setContent(resultHtml);
                    postSourcePanel.setText(resultHtml);
                    preSourcePanel.setText(resultJs);
                    appendLog("Template ran successfully: " + editorTabPanel.getSelectedFileName());
                });
            }
        } catch (RendererException ex) {
            appendLog("An error occurred: " + ex.getMessage());
            errorLogPanel.addError(ex);
            preSourcePanel.setText(ex.getSourceCode());
        } catch (Exception ex) {
            ex.printStackTrace();
            showAlert("Error running template: " + ex.getMessage());
            appendLog("Error running template: " + ex.getMessage());
        }
    }

    /**
     * Shows an alert dialog with the specified message
     *
     * @param msg the message to show
     */
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

    private void reportError(String message, boolean displayAlert) {
        logPanel.appendLog(message);
        if (displayAlert) {
            showAlert(message);
        }
    }

    private void reportError(String message, Exception exception, boolean showAlert) {
        reportError(message, showAlert);
        errorLogPanel.addError(exception);

    }

    private void showAbout() {

    }
}
