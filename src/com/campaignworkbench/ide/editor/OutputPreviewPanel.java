package com.campaignworkbench.ide.editor;

import com.campaignworkbench.ide.IDETheme;
import com.campaignworkbench.ide.ThemeManager;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebView;

public class OutputPreviewPanel implements  IJavaFxNode {

    private WebView preview;
    private VBox topPreviewBox;
    Label outputLabel;

    public OutputPreviewPanel() {
        preview = new WebView();
        preview.setCursor(Cursor.TEXT);

        outputLabel = new Label("Output");
        topPreviewBox = new VBox(5, outputLabel, preview);
    }

    public void setContent(String content) {
        preview.getEngine().loadContent(content);
    }

    @Override
    public Node getNode() {
        return topPreviewBox;
    }
}
