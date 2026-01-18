package com.campaignworkbench.ide.editor;

import com.campaignworkbench.ide.IDETheme;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

public class SourcePreviewPanel extends RSyntaxEditor implements IJavaFxNode {

    VBox bottomPreviewBox;
    Label jsLabel;

    public SourcePreviewPanel() {
        super();
        jsLabel = new Label("JavaScript");
        bottomPreviewBox = new VBox(5, jsLabel, super.getNode());
        super.setEditable(true);
        super.setSyntax(SyntaxType.SOURCE_PREVIEW);
    }

    public Node getNode() {
        return bottomPreviewBox;
    }
}
