package com.campaignworkbench.ide.editor;

import com.campaignworkbench.ide.IThemeable;
import com.campaignworkbench.ide.IDETheme;
import com.campaignworkbench.ide.ThemeManager;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebView;

public class OutputPreviewPanel implements IThemeable, IJavaFxNode {

    private WebView preview;
    private VBox topPreviewBox;
    Label outputLabel;

    public OutputPreviewPanel() {
        preview = new WebView();
        preview.setCursor(Cursor.TEXT);

        outputLabel = new Label("Output");
        topPreviewBox = new VBox(5, outputLabel, preview);

        ThemeManager.register(this);
    }

    public void setContent(String content) {
        preview.getEngine().loadContent(content);
    }

    @Override
    public void applyTheme(IDETheme theme) {
        switch (theme) {
            case DARK:
                topPreviewBox.setStyle("-fx-background-color: #2b2b2b;");
                outputLabel.setStyle("-fx-text-fill: white;");
                preview.setStyle("-fx-background-color: #2b2b2b;");
                break;
            case LIGHT:
            default:
                topPreviewBox.setStyle("-fx-background-color: #ececec;");
                outputLabel.setStyle("-fx-text-fill: black;");
                preview.setStyle("-fx-background-color: #ececec;");
                break;
        }
    }

    @Override
    public Node getNode() {
        return topPreviewBox;
    }
}
