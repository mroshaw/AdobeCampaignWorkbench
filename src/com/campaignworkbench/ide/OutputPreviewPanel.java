package com.campaignworkbench.ide;

import javafx.geometry.Insets;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebView;

/**
 * Implements an HTML preview panel for use in the IDE User Interface
 */
public class OutputPreviewPanel implements IJavaFxNode {

    private final WebView preview;
    private final VBox topPreviewBox;
    /**
     * The label for the preview panel
     */
    Label outputLabel;

    /**
     * Constructor
     * @param label The label for the preview panel
     */
    public OutputPreviewPanel(String label) {
        preview = new WebView();
        preview.setCursor(Cursor.TEXT);

        outputLabel = new Label(label);
        outputLabel.setPadding(new Insets(0,0, 0,5));
        outputLabel.setStyle("-fx-font-weight: bold;");

        topPreviewBox = new VBox(5, outputLabel, preview);
        topPreviewBox.setPadding(new Insets(0,0, 0,5));
        
        VBox.setVgrow(preview, Priority.ALWAYS);
    }

    /**
     * @param content HTML preview content to set in the UI
     */
    public void setContent(String content) {
        preview.getEngine().loadContent(content);
    }

    @Override
    public Node getNode() {
        return topPreviewBox;
    }
}
