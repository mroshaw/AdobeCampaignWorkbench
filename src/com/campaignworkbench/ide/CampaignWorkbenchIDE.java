package com.campaignworkbench.ide;

import com.campaignworkbench.campaignrenderer.TemplateParseException;
import com.campaignworkbench.campaignrenderer.TemplateRenderResult;
import com.campaignworkbench.campaignrenderer.TemplateRenderer;
import com.campaignworkbench.ide.editor.RSyntaxEditor;
import com.campaignworkbench.ide.editor.SyntaxType;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.web.WebView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class CampaignWorkbenchIDE extends Application {

    private String workspacePath = "Workspaces/Test Workspace";

    private TabPane tabPane;
    private WebView preview;
    private RSyntaxEditor sourcePreview;
    private File xmlContextFile;
    private String xmlContextContent;
    private Label xmlContextLabel;
    private Button runButton;
    private TextArea logArea; // full-width log pane

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Campaign Workbench");

        tabPane = new TabPane();

        // --- Preview panes ---
        preview = new WebView();
        preview.setCursor(Cursor.TEXT);

        sourcePreview = new RSyntaxEditor();
        sourcePreview.setEditable(true);
        sourcePreview.setSyntax(SyntaxType.SOURCE_PREVIEW);

        Label outputLabel = new Label("Output");
        VBox topPreviewBox = new VBox(5, outputLabel, preview);

        Label jsLabel = new Label("JavaScript");
        VBox bottomPreviewBox = new VBox(5, jsLabel, sourcePreview.getNode());

        SplitPane previewSplitPane = new SplitPane();
        previewSplitPane.setOrientation(Orientation.VERTICAL);
        previewSplitPane.getItems().addAll(topPreviewBox, bottomPreviewBox);
        previewSplitPane.setDividerPositions(0.7);

        // --- Right pane (holds preview split) ---
        BorderPane rightPane = new BorderPane();
        rightPane.setCenter(previewSplitPane);

        // --- Main horizontal split (editor tabs | preview) ---
        SplitPane mainSplitPane = new SplitPane();
        mainSplitPane.setOrientation(Orientation.HORIZONTAL);
        mainSplitPane.getItems().addAll(tabPane, rightPane);
        mainSplitPane.setDividerPositions(0.5);

        tabPane.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldTab, newTab) -> updateRunButtonState(newTab)
        );

        MenuBar menuBar = createMenu();
        HBox toolbar = createToolbar();
        VBox topBar = new VBox(menuBar, toolbar);

        // --- Full-width log pane ---
        Label logLabel = new Label("Logs");
        logArea = new TextArea();
        logArea.setEditable(false);
        logArea.setWrapText(true);
        logArea.setCursor(Cursor.TEXT); // full-width log pane
        logArea.setFont(Font.font("Source Code Pro", 14));

        VBox logBox = new VBox(5, logLabel, logArea);
        // logBox.setPrefHeight(120);
        VBox.setVgrow(logArea, Priority.ALWAYS);
        SplitPane rootSplitPane = new SplitPane();
        rootSplitPane.setOrientation(Orientation.VERTICAL);
        rootSplitPane.getItems().addAll(mainSplitPane, logBox); // logBox = VBox with logLabel + logArea
        rootSplitPane.setDividerPositions(0.8); // 80% main area, 20% log initially

        // --- Root BorderPane ---
        BorderPane root = new BorderPane();
        root.setTop(topBar);
        root.setCenter(rootSplitPane);
        // root.setBottom(logBox);

        Scene scene = new Scene(root, 1000, 600);

        primaryStage.setScene(scene);
        primaryStage.show();

        appendLog("Welcome to Campaign workbench! Load a template, select an XML context, and hit run!");
    }

    private HBox createToolbar() {
        // --- Top toolbar ---
        Button openTemplateButton = new Button("Open Template");
        Button openBlockButton = new Button("Open Block");
        Button openXmlButton = new Button("Open XML");
        Button setXmlButton = new Button("Set XML Context");

        xmlContextLabel = new Label("No XML context set");

        runButton = new Button();
        runButton.setTooltip(new Tooltip("Run Template"));
        Label arrowLabel = new Label("▶");
        arrowLabel.setStyle("-fx-text-fill: green; -fx-font-size: 18px;");
        runButton.setGraphic(arrowLabel);
        runButton.setDisable(true);

        openTemplateButton.setOnAction(e -> openFile(workspacePath + "/Templates"));
        openBlockButton.setOnAction(e -> openFile(workspacePath + "/PersoBlocks"));
        openXmlButton.setOnAction(e -> openFile(workspacePath + "/XmlContext"));
        setXmlButton.setOnAction(e -> setXmlContext());
        runButton.setOnAction(e -> runTemplate());

        HBox toolBar = new HBox(
                10,
                openTemplateButton,
                // openBlockButton,
                // openXmlButton,
                setXmlButton,
                xmlContextLabel,
                runButton
        );
        toolBar.setAlignment(Pos.CENTER_LEFT);
        return toolBar;
    }

    private MenuBar createMenu() {
        MenuBar menuBar = new MenuBar();

        // --- File Menu ---
        Menu fileMenu = new Menu("File");

        // Open submenu
        Menu openSub = new Menu("Open");
        MenuItem openWorkspace = new MenuItem("Workspace");
        MenuItem openTemplate = new MenuItem("Template");
        MenuItem openBlock = new MenuItem("Block");
        MenuItem openXmlContext = new MenuItem("XML Context");

        openWorkspace.setOnAction(e -> dummyHandler("Open Workspace"));
        openTemplate.setOnAction(e -> openFile(workspacePath + "/Templates"));
        openBlock.setOnAction(e -> openFile(workspacePath + "/PersoBlocks"));
        openXmlContext.setOnAction(e -> openFile(workspacePath + "/XmlContext"));

        openSub.getItems().addAll(openWorkspace, openTemplate, openBlock, openXmlContext);

        // New submenu
        Menu newSub = new Menu("New");
        MenuItem newWorkspace = new MenuItem("Workspace");
        MenuItem newTemplate = new MenuItem("Template");
        MenuItem newBlock = new MenuItem("Block");
        MenuItem newXmlContext = new MenuItem("XML Context");

        newWorkspace.setOnAction(e -> dummyHandler("New Workspace"));
        newTemplate.setOnAction(e -> dummyHandler("New Template"));
        newBlock.setOnAction(e -> dummyHandler("New Block"));
        newXmlContext.setOnAction(e -> dummyHandler("New XML Context"));

        newSub.getItems().addAll(newWorkspace, newTemplate, newBlock, newXmlContext);

        // Save items
        MenuItem saveCurrent = new MenuItem("Save Current");
        saveCurrent.setAccelerator(
                new KeyCodeCombination(KeyCode.S, KeyCombination.CONTROL_DOWN)
        );
        MenuItem saveCurrentAs = new MenuItem("Save Current As");
        MenuItem exitItem = new MenuItem("Exit");

        saveCurrent.setOnAction(e -> saveCurrent());
        saveCurrentAs.setOnAction(e -> dummyHandler("Save Current As"));
        exitItem.setOnAction(e -> Platform.exit());

        fileMenu.getItems().addAll(openSub, newSub, saveCurrent, saveCurrentAs, new SeparatorMenuItem(), exitItem);

        // --- Help Menu ---
        Menu helpMenu = new Menu("Help");
        MenuItem aboutItem = new MenuItem("About");
        aboutItem.setOnAction(e -> dummyHandler("About"));
        helpMenu.getItems().add(aboutItem);

        Menu viewMenu = new Menu("View");
        MenuItem darkThemeItem = new MenuItem("Dark Theme");
        darkThemeItem.setOnAction(e -> {
            if (tabPane.getSelectionModel().getSelectedItem() instanceof EditorTab) {
                EditorTab editorTab = (EditorTab) tabPane.getSelectionModel().getSelectedItem();
                editorTab.getEditor().applyDarkTheme(true);
            }
        });
        viewMenu.getItems().add(darkThemeItem);

        MenuItem lightThemeItem = new MenuItem("Light Theme");
        lightThemeItem.setOnAction(e -> {
            if (tabPane.getSelectionModel().getSelectedItem() instanceof EditorTab) {
                EditorTab editorTab = (EditorTab) tabPane.getSelectionModel().getSelectedItem();
                editorTab.getEditor().applyDarkTheme(false);
            }
        });
        viewMenu.getItems().add(lightThemeItem);


        // --- Add menus to menu bar ---
        menuBar.getMenus().addAll(fileMenu, viewMenu, helpMenu);

        return menuBar;
    }

    private void dummyHandler(String action) {
        appendLog("Menu action triggered: " + action);
    }

    private void updateRunButtonState(Tab tab) {
        if (tab instanceof EditorTab) {
            EditorTab editorTab = (EditorTab) tab;
            String name = editorTab.getFile()
                    .getFileName()
                    .toString()
                    .toLowerCase();
            runButton.setDisable(!name.endsWith(".template"));
        } else {
            runButton.setDisable(true);
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

        File selectedFile = fileChooser.showOpenDialog(
                tabPane.getScene().getWindow()
        );

        if (selectedFile != null && selectedFile.getName().endsWith(".xml")) {
            xmlContextFile = selectedFile;
            xmlContextLabel.setText(selectedFile.getName());
            try {
                xmlContextContent = Files.readString(xmlContextFile.toPath());
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        }
    }

    private void openFile(String defaultSubfolder) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open File");
        fileChooser.setInitialDirectory(
                new File(System.getProperty("user.dir"), defaultSubfolder)
        );

        File selectedFile = fileChooser.showOpenDialog(
                tabPane.getScene().getWindow()
        );
        if (selectedFile == null) return;

        try {
            Path path = selectedFile.toPath();
            String content = Files.readString(path);

            EditorTab tab = new EditorTab(path, content);
            tab.setClosable(true);

            tabPane.getTabs().add(tab);
            tabPane.getSelectionModel().select(tab);

        } catch (IOException ex) {
            ex.printStackTrace();
            showAlert("Failed to open file: " + ex.getMessage());
        }
    }

    private void saveCurrent() {
        Tab selectedTab = tabPane.getSelectionModel().getSelectedItem();

        if (!(selectedTab instanceof EditorTab)) {
            appendLog("No editor tab selected to save.");
            showAlert("No editor tab selected.");
            return;
        }

        EditorTab editorTab = (EditorTab) selectedTab;
        Path file = editorTab.getFile();
        String content = editorTab.getEditorText();

        try {
            Files.writeString(file, content);
            appendLog("Saved file: " + file.getFileName());
        } catch (IOException e) {
            appendLog("Failed to save file: " + file.getFileName());
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

        Tab selectedTab = tabPane.getSelectionModel().getSelectedItem();
        if (!(selectedTab instanceof EditorTab)) {
            showAlert("No template tab selected!");
            appendLog("No template tab selected!");
            return;
        }

        EditorTab editorTab = (EditorTab) selectedTab;

        try {
            String templateSource = editorTab.getEditorText();

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
                    editorTab.getFile().getFileName().toString()
            );

            String resultHtml = renderResult.getRenderedOutput();
            String resultJs = renderResult.getGeneratedJavaScript();

            Platform.runLater(() -> {
                preview.getEngine().loadContent(resultHtml);
                sourcePreview.setText(resultJs);
                appendLog("Template ran successfully: " + editorTab.getFile().getFileName());
            });
        } catch (TemplateParseException parseEx) {
            appendLog("An error occurred parsing the template: " + parseEx.getTemplateName() + " at line: " + parseEx.getTemplateLine());
            appendLog(parseEx.getMessage());
            Throwable cause = parseEx.getCause();
            while (cause != null) {
                appendLog("Caused by: " + cause.getClass().getSimpleName() + " : " + cause.getMessage());
                cause = cause.getCause();
            }
        } catch (Exception ex) {
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

    private void appendLog(String msg) {
        Platform.runLater(() -> logArea.appendText(msg + "\n"));
    }
}
