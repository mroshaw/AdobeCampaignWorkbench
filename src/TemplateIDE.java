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

import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.LineNumberFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;

public class TemplateIDE extends Application {

    private TabPane tabPane;
    private WebView preview;
    private File xmlContextFile; // currently active XML context
    private String xmlContextContent;
    private Label xmlContextLabel; // shows which XML is active
    Button runButton;
    private Boolean isTemplateSelected;

    // Map to track which files are open in which tabs
    private final Map<Tab, File> openFiles = new HashMap<>();

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Template IDE");

        // --- Tabs for open files ---
        tabPane = new TabPane();

        // --- Web preview ---
        preview = new WebView();

        // --- Split pane for editor / preview ---
        SplitPane splitPane = new SplitPane();
        splitPane.setOrientation(Orientation.HORIZONTAL);
        splitPane.getItems().addAll(tabPane, preview);
        splitPane.setDividerPositions(0.5);

        // --- Top bar buttons ---
        Button openTemplateButton = new Button("Open Template");
        Button openBlockButton = new Button("Open Block");
        Button openXmlButton = new Button("Open XML");
        Button setXmlButton = new Button("Set XML Context");
        xmlContextLabel = new Label("No XML context set");
        runButton = new Button("Run Template");
        runButton.setDisable(true);

        // --- Open Template ---
        openTemplateButton.setOnAction(e -> openFile("Templates"));

        // --- Open Block ---
        openBlockButton.setOnAction(e -> openFile("PersoBlocks"));

        // --- Open XML ---
        openXmlButton.setOnAction(e -> openFile("XmlContext"));

        tabPane.getSelectionModel().selectedItemProperty().addListener((obs, oldTab, newTab) -> {
            updateRunButtonState(newTab);
        });

        // --- Set XML context ---
        setXmlButton.setOnAction(e -> setXmlContext());

        // --- Run template ---
        runButton.setOnAction(e -> runTemplate());

        HBox topBar = new HBox(10, openTemplateButton, openBlockButton, openXmlButton,
                setXmlButton, xmlContextLabel, runButton);

        // --- Main layout ---
        BorderPane root = new BorderPane();
        root.setTop(topBar);
        root.setCenter(splitPane);

        Scene scene = new Scene(root, 1000, 600);
        // --- Load your existing CSS for highlighting ---
        scene.getStylesheets().add(getClass().getResource("template-style.css").toExternalForm());

        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void updateRunButtonState(Tab tab) {
        File file = openFiles.get(tab);
        if (file != null && (file.getName().endsWith(".block") || file.getName().endsWith(".template"))) {
            runButton.setDisable(false);
            isTemplateSelected = true;
        } else {
            runButton.setDisable(true);
            isTemplateSelected = false;
        }
    }

    private void setXmlContext() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open File");
        fileChooser.setInitialDirectory(new File(System.getProperty("user.dir"), "XmlContext"));
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("All Files", "*.*"),
                new FileChooser.ExtensionFilter("XML Files", "*.xml"),
                new FileChooser.ExtensionFilter("Template / Block Files", "*.tpl", "*.block")
        );

        File selectedFile = fileChooser.showOpenDialog(tabPane.getScene().getWindow());

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

    // --- File opening ---
    private void openFile(String defaultSubfolder) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open File");
        fileChooser.setInitialDirectory(new File(System.getProperty("user.dir"), defaultSubfolder));
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("All Files", "*.*"),
                new FileChooser.ExtensionFilter("XML Files", "*.xml"),
                new FileChooser.ExtensionFilter("Template / Block Files", "*.tpl", "*.block")
        );

        File selectedFile = fileChooser.showOpenDialog(tabPane.getScene().getWindow());
        if (selectedFile != null) {
            try {
                String content = Files.readString(selectedFile.toPath());
                CodeArea codeArea = new CodeArea();
                codeArea.setParagraphGraphicFactory(LineNumberFactory.get(codeArea));
                codeArea.replaceText(content);

                // --- APPLY SYNTAX HIGHLIGHTING ---
                SyntaxHighlighter.applyHighlighting(codeArea);

                Tab tab = new Tab(selectedFile.getName(), codeArea);
                tab.setClosable(true);
                openFiles.put(tab, selectedFile);
                tabPane.getTabs().add(tab);
                tabPane.getSelectionModel().select(tab);
            } catch (Exception ex) {
                ex.printStackTrace();
                showAlert("Failed to open file: " + ex.getMessage());
            }
        }
    }

    // --- Template execution ---
    private void runTemplate() {
        if (xmlContextFile == null) {
            showAlert("No XML context file set!");
            return;
        }

        try {
            Tab selectedTab = tabPane.getSelectionModel().getSelectedItem();
            if (selectedTab == null) {
                showAlert("No template tab selected!");
                return;
            }

            File templateFile = openFiles.get(selectedTab);
            if (templateFile == null) return;

            String templateSource = ((CodeArea) selectedTab.getContent()).getText();


            // --- Rhino context setup ---
            Context cx = Context.enter();
            cx.setOptimizationLevel(-1); // disable some JIT issues
            Scriptable scope = cx.initStandardObjects();

            // --- SIMPLE XML CONTEXT ---
            // Just wrap the XML in a JavaScript XML() literal
            cx.evaluateString(
                    scope,
                    "var rtEvent = new XML(`" + xmlContextContent + "`);",
                    xmlContextFile.getName(),
                    1,
                    null
            );

            // XML context is passed exactly as before
            scope.put("xmlContext", scope, xmlContextContent);
            String resultHtml = TemplateRenderer.render(templateSource, cx, scope, templateFile.getName());

            Platform.runLater(() -> preview.getEngine().loadContent(resultHtml));
        } catch (Exception ex) {
            ex.printStackTrace();
            showAlert("Error running template: " + ex.getMessage());
        } finally {
            Context.exit();
        }
    }

    private void showAlert(String msg) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.WARNING, msg, ButtonType.OK);
            alert.showAndWait();
        });
    }
}
