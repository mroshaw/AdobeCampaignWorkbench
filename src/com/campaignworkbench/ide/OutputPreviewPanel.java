package com.campaignworkbench.ide;

import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebView;

/**
 * Implements an HTML preview panel for use in the IDE User Interface
 */
public class OutputPreviewPanel implements IJavaFxNode {

    private final WebView preview;
    private final VBox topPreviewBox;
    Label outputLabel;

    /**
     * Constructor
     */
    public OutputPreviewPanel() {
        preview = new WebView();
        preview.setCursor(Cursor.TEXT);

        outputLabel = new Label("Output");
        topPreviewBox = new VBox(5, outputLabel, preview);
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
