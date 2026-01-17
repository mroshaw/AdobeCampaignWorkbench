package com.campaignworkbench.ide.editor;

import com.campaignworkbench.ide.IDETheme;
import com.campaignworkbench.ide.IThemeable;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

public class SourcePreviewPanel extends RSyntaxEditor implements IJavaFxNode, IThemeable {

    VBox bottomPreviewBox;
    Label jsLabel;

    public SourcePreviewPanel() {
        super();
        jsLabel = new Label("JavaScript");
        bottomPreviewBox = new VBox(5, jsLabel, super.getNode());
        super.setEditable(true);
        super.setSyntax(SyntaxType.SOURCE_PREVIEW);
    }

    public void applyTheme(IDETheme theme) {
        super.applyTheme(theme);
        switch (theme) {
            case DARK:
                bottomPreviewBox.setStyle("-fx-background-color: #2b2b2b;");
                jsLabel.setStyle("-fx-text-fill: white;");
                break;
            case LIGHT:
            default:
                bottomPreviewBox.setStyle("-fx-background-color: #ececec;");
                jsLabel.setStyle("-fx-text-fill: black;");
                break;
        }
    }

    public Node getNode() {
        return bottomPreviewBox;
    }
}
