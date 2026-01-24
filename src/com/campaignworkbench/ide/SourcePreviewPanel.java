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

    /**
     * The panel containing the source preview
     */
    VBox sourcePreviewPanel;
    /**
     * The label for the source preview panel
     */
    Label sourcePreviewLabel;

    /**
     * Constructor
     * @param label The label for the source preview panel
     * @param syntaxType The syntax highlighting type to use
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
