package com.campaignworkbench.ide;

import com.campaignworkbench.campaignrenderer.TemplateRenderResult;
import com.campaignworkbench.campaignrenderer.TemplateRenderer;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Orientation;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
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
    private File xmlContextFile;
    private String xmlContextContent;
    private Label xmlContextLabel;
    private Button runButton;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Template IDE");

        tabPane = new TabPane();
        preview = new WebView();

        SplitPane splitPane = new SplitPane();
        splitPane.setOrientation(Orientation.HORIZONTAL);
        splitPane.getItems().addAll(tabPane, preview);
        splitPane.setDividerPositions(0.5);

        Button openTemplateButton = new Button("Open Template");
        Button openBlockButton = new Button("Open Block");
        Button openXmlButton = new Button("Open XML");
        Button setXmlButton = new Button("Set XML Context");

        xmlContextLabel = new Label("No XML context set");

        runButton = new Button("Run Template");
        runButton.setDisable(true);

        openTemplateButton.setOnAction(e -> openFile(workspacePath + "/Templates"));
        openBlockButton.setOnAction(e -> openFile(workspacePath + "/PersoBlocks"));
        openXmlButton.setOnAction(e -> openFile(workspacePath + "/XmlContext"));
        setXmlButton.setOnAction(e -> setXmlContext());

        tabPane.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldTab, newTab) -> updateRunButtonState(newTab)
        );

        runButton.setOnAction(e -> runTemplate());

        HBox topBar = new HBox(
                10,
                openTemplateButton,
                openBlockButton,
                openXmlButton,
                setXmlButton,
                xmlContextLabel,
                runButton
        );

        BorderPane root = new BorderPane();
        root.setTop(topBar);
        root.setCenter(splitPane);

        Scene scene = new Scene(root, 1000, 600);

        primaryStage.setScene(scene);
        primaryStage.show();
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

    private void runTemplate() {
        if (xmlContextFile == null) {
            showAlert("No XML context file set!");
            return;
        }

        Tab selectedTab = tabPane.getSelectionModel().getSelectedItem();
        if (!(selectedTab instanceof EditorTab)) {
            showAlert("No template tab selected!");
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

            Platform.runLater(() ->
                    preview.getEngine().loadContent(resultHtml)
            );

        } catch (Exception ex) {
            ex.printStackTrace();
            showAlert("Error running template: " + ex.getMessage());
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
