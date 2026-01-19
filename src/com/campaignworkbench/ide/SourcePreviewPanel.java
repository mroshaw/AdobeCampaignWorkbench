package com.campaignworkbench.ide;

import com.campaignworkbench.ide.editor.RSyntaxEditor;
import com.campaignworkbench.ide.editor.SyntaxType;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

/**
 * Implements a User Interface control to preview the JavaScript source generated
 */
public class SourcePreviewPanel extends RSyntaxEditor implements IJavaFxNode {

    VBox bottomPreviewBox;
    Label jsLabel;

    /**
     * Constructor
     */
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
