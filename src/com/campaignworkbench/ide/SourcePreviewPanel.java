package com.campaignworkbench.ide;

import com.campaignworkbench.ide.editor.RSyntaxEditor;
import com.campaignworkbench.ide.editor.SyntaxType;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

/**
 * Implements a User Interface control to preview the JavaScript source generated
 */
public class SourcePreviewPanel extends RSyntaxEditor implements IJavaFxNode {

    VBox sourcePreviewPanel;
    Label sourcePreviewLabel;

    /**
     * Constructor
     */
    public SourcePreviewPanel(String label, SyntaxType syntaxType) {
        super();
        sourcePreviewLabel = new Label(label);
        sourcePreviewLabel.setPadding(new Insets(0,0, 0,5));
        sourcePreviewLabel.setStyle("-fx-font-weight: bold;");
        sourcePreviewPanel = new VBox(5, sourcePreviewLabel, super.getNode());
        sourcePreviewPanel.setPadding(new Insets(0,0, 0,5));

        super.setEditable(true);
        super.setSyntax(syntaxType);
    }

    public Node getNode() {
        return sourcePreviewPanel;
    }
}
