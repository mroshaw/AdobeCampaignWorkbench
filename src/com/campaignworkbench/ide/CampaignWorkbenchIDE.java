package com.campaignworkbench.ide;

import com.campaignworkbench.campaignrenderer.*;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Orientation;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.SplitPane;
import javafx.scene.control.Tab;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.Objects;

/**
 * Builds a User Interface for the Campaign Workbench IDE
 */
public class CampaignWorkbenchIDE extends Application implements IThemeable {

    /**
     * The current active workspace
     */
    private WorkspaceExplorer workspaceExplorer;
    private MainToolBar toolBar;
    private EditorTabPanel editorTabPanel;
    private LogPanel logPanel;
    private ErrorLogPanel errorLogPanel;
    private OutputTabPanel outputPanel;
    private EditorTab currentEditorTab;
    private Scene scene;
    private Image iconImage;

    /**
     * Main entry point for the application
     *
     * @param args command line arguments
     */
    static void main(String[] args) {
        launch(args);
    }


    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Campaign Workbench");

        // Set the icon
        iconImage = new Image(
                Objects.requireNonNull(getClass().getResourceAsStream("/app.png")));
        primaryStage.getIcons().add(iconImage);

        // Menu and toolbar
        MainMenuBar menuBar = new MainMenuBar(
                _ -> newWorkspaceHandler(),
                _ -> openWorkspaceHandler(),
                _ -> saveWorkspaceHandler(),
                _ -> closeWorkspaceHandler(),
                _ -> createNewFileHandler(WorkspaceFileType.TEMPLATE),
                _ -> createNewFileHandler(WorkspaceFileType.MODULE),
                _ -> createNewFileHandler(WorkspaceFileType.BLOCK),
                _ -> createNewFileHandler(WorkspaceFileType.CONTEXT),

                _ -> addExistingFileHandler(WorkspaceFileType.TEMPLATE),
                _ -> addExistingFileHandler(WorkspaceFileType.MODULE),
                _ -> addExistingFileHandler(WorkspaceFileType.BLOCK),
                _ -> addExistingFileHandler(WorkspaceFileType.CONTEXT),

                _ -> saveCurrentFileHandler(),
                _ -> saveCurrentFileAsHandler(),
                _ -> toggleFindHandler(),

                _ -> setThemeHandler(IDETheme.LIGHT),
                _ -> setThemeHandler(IDETheme.DARK),

                _ -> showAbout(),
                _ -> exitApplication()
        );

        toolBar = new MainToolBar(_ -> openWorkspaceHandler(),
                _ -> newWorkspaceHandler(),
                _ -> closeWorkspaceHandler(),
                _ -> closeEditorTabsHandler(),
                _ -> runTemplate());

        // Workspace Explorer
        workspaceExplorer = new WorkspaceExplorer("Workspace Explorer", null, this::openFileFromWorkspace, this::workspaceChanged);

        // Editor tabs
        editorTabPanel = new EditorTabPanel((_, _, newTab) -> tabPanelChanged(newTab));

        // Output panes
        outputPanel = new OutputTabPanel();

        // Log pane
        logPanel = new LogPanel("Logs");
        errorLogPanel = new ErrorLogPanel("Errors");

        errorLogPanel.setOnErrorDoubleClicked((workspaceFile, line) -> outputPanel.highlightJsLine(line));

        SplitPane logSplitPane = new SplitPane();
        logSplitPane.setOrientation(Orientation.HORIZONTAL);
        logSplitPane.getItems().addAll(logPanel.getNode(), errorLogPanel.getNode());
        logSplitPane.setDividerPositions(0.5);

        // --- Split: Workspace Explorer | Editor Tabs ---
        SplitPane workspaceEditorSplit = new SplitPane();
        workspaceEditorSplit.setOrientation(Orientation.HORIZONTAL);
        workspaceEditorSplit.getItems().addAll(
                workspaceExplorer.getNode(),
                editorTabPanel.getNode()
        );
        workspaceEditorSplit.setDividerPositions(0.28);
        SplitPane.setResizableWithParent(workspaceExplorer.getNode(), false);

        // --- Split: (Workspace+Editor) | Preview ---
        SplitPane editorPreviewSplit = new SplitPane();
        editorPreviewSplit.setOrientation(Orientation.HORIZONTAL);
        editorPreviewSplit.getItems().addAll(
                workspaceEditorSplit,
                outputPanel.getNode()
        );
        editorPreviewSplit.setDividerPositions(0.72);
        // SplitPane.setResizableWithParent(previewSplitPane, false);

        VBox topBar = new VBox(menuBar.getNode(), toolBar.getNode());

        SplitPane rootSplitPane = new SplitPane();
        rootSplitPane.setOrientation(Orientation.VERTICAL);
        rootSplitPane.getItems().addAll(editorPreviewSplit, logSplitPane); // logBox = VBox with logLabel + logArea
        rootSplitPane.setDividerPositions(0.8);

        // --- Root BorderPane ---
        BorderPane root = new BorderPane();
        root.setTop(topBar);
        root.setCenter(rootSplitPane);
        scene = new Scene(root, 1600, 900);
        primaryStage.setScene(scene);
        primaryStage.show();
        ThemeManager.register(this);
        Workspace.createWorkspaceRootFolder();
        appendLog("Welcome to Campaign workbench!");
    }

    /**
     * Stop the application
     */
    @Override
    public void stop() {
        exitApplication();
    }

    private void exitApplication() {
        workspaceExplorer.saveWorkspace();
        editorTabPanel.closeAllTabs();
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

    private void setThemeHandler(IDETheme ideTheme) {
        ThemeManager.setTheme(ideTheme);
    }

    /**
     * Applies a theme to the IDE
     *
     */
    @Override
    public void applyTheme(IDETheme ideTheme) {

        // Set AtlantaFX styles
        Application.setUserAgentStylesheet(ideTheme.getAtlantaFxStyleSheet());

        // Set IDE theme styles
        scene.getStylesheets().add(ideTheme.getIdeStyleSheet());
    }

    /**
     * Updates the run button state based on the current tab
     *
     * @param tab the currently selected tab
     */
    private void tabPanelChanged(Tab tab) {
        if (tab instanceof EditorTab editorTab) {
            toolBar.setRunButtonState(editorTab.isTemplateTab());
            currentEditorTab = editorTab;
        }
    }

    /**
     * Opens a workspace directory
     */
    private void openWorkspaceHandler() {
        try {
            workspaceExplorer.openWorkspace();
        } catch (IDEException ideEx) {
            reportError("An error occurred while opening the workspace: " + ideEx.getMessage(), ideEx, true);
        }
    }

    private void newWorkspaceHandler() {
        try {
            workspaceExplorer.createNewWorkspace();
        } catch (IDEException ideEx) {
            reportError("An error occurred while creating a new workspace: " + ideEx.getMessage(), ideEx, true);
        }
    }

    private void saveWorkspaceHandler() {
        try {
            workspaceExplorer.saveWorkspace();
        } catch (IDEException ideEx) {
            reportError("An error occurred while saving the workspace: " + ideEx.getMessage(), ideEx, true);
        }
    }

    private void closeWorkspaceHandler() {
        try {
            editorTabPanel.closeAllTabs();
            workspaceExplorer.closeWorkspace();
        } catch (IDEException ideEx) {
            reportError("An error occurred while closing the workspace: " + ideEx.getMessage(), ideEx, true);
        }
    }

    private void closeEditorTabsHandler() {
        editorTabPanel.closeAllTabs();
    }

    private void saveWorkspaceAs() {
        try {
            workspaceExplorer.saveWorkspace();
        } catch (IDEException ideEx) {
            reportError("An error occurred while saving the workspace: " + ideEx.getMessage(), ideEx, true);
        }
    }

    /**
     * Opens a file from the workspace explorer
     *
     * @param workspaceFile the file to open
     */
    private void openFileFromWorkspace(WorkspaceFile workspaceFile) {
        openFileInNewTab(workspaceFile);
    }

    private void workspaceChanged(Workspace newWorkspace) {
        editorTabPanel.closeAllTabs();
    }

    /**
     * Opens a file from the file system
     *
     */
    private void addExistingFileHandler(WorkspaceFileType fileType) {
        try {
            workspaceExplorer.addExistingFile(fileType);
        } catch (IDEException ideEx) {
            reportError("An error occurred while adding an existing file of type: " + fileType, ideEx, true);
        }
    }

    private void createNewFileHandler(WorkspaceFileType fileType) {
        try {
            workspaceExplorer.createNewFile(fileType);
            saveWorkspaceHandler();
        } catch (IDEException ideEx) {
            reportError("An error occurred while creating an new file of type: " + fileType, ideEx, true);
        }
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
    private void saveCurrentFileHandler() {

        if (!editorTabPanel.isSelected()) {
            appendLog("No editor tab selected to save.");
            showAlert("No editor tab selected.");
            return;
        }
        editorTabPanel.saveSelectedTab();
    }

    private void saveCurrentFileAsHandler() {

    }

    private void toggleFindHandler() {
        if (currentEditorTab != null) {
            currentEditorTab.toggleFind();
        }
    }

    /**
     * Runs the template in the currently selected editor tab
     */
    private void runTemplate() {
        if(!workspaceExplorer.isWorkspaceOpen()) {
            reportError("No workspace is open. Please open a workspace before running a template.", true);
            return;
        }

        errorLogPanel.clearErrors();
        saveWorkspaceHandler();
        try {
            WorkspaceFile selectedWorkspaceFile = editorTabPanel.getSelectedWorkspaceFile();

            if (selectedWorkspaceFile instanceof Template workspaceContextFile) {

                TemplateRenderResult renderResult = TemplateRenderer.render(
                        workspaceExplorer.getWorkspace(),
                        workspaceContextFile
                );

                String resultHtml = renderResult.renderedOutput();
                String resultJs = renderResult.generatedJavaScript();

                Platform.runLater(() -> {
                    outputPanel.setContent(resultHtml, resultJs);
                    appendLog("Template ran successfully: " + editorTabPanel.getSelectedFileName());
                });
            }
        } catch (IDEException ideEx) {
            appendLog("An IDE error occurred: " + ideEx.getMessage());
            reportError(ideEx.getMessage(), ideEx, true);
        } catch (RendererException renderEx) {
            appendLog("A Renderer error occurred: " + renderEx.getMessage());
            errorLogPanel.addError(renderEx);
            outputPanel.setContent("", renderEx.getSourceCode());
        } catch (Exception ex) {
            appendLog("An unexpected error occurred: " + ex.getMessage());
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
        AboutBox.show(scene.getWindow(), "Campaign Workbench", "v1.0.0", "Powerful IDE to build and test Campaign Classic template code", "©2026 Specsavers", iconImage);
    }
}
