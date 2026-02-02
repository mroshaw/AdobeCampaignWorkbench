package com.campaignworkbench.ide;

import com.campaignworkbench.ide.editor.*;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

/**
 * Implements a User Interface control to preview the JavaScript source generated
 */
public class SourcePreviewPanel implements IJavaFxNode {

    private final ICodeEditor editor;
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
        // this.editor = new MonacoFXEditor();
        // this.editor = new RichTextFXEditor();
        this.editor = new RSyntaxEditor();
        sourcePreviewLabel = new Label(label);
        sourcePreviewLabel.setPadding(new Insets(0,0, 0,5));
        // sourcePreviewLabel.setStyle("-fx-font-weight: bold;");
        sourcePreviewPanel = new VBox(5, sourcePreviewLabel, editor.getNode());
        sourcePreviewPanel.setPadding(new Insets(0,0, 0,5));
        sourcePreviewPanel.setMinHeight(0);
        
        VBox.setVgrow(editor.getNode(), Priority.ALWAYS);

        editor.setEditable(false);
        editor.setSyntax(syntaxType);
    }

    public void setText(String text) {
        editor.setText(text);
    }

    public Node getNode() {
        return sourcePreviewPanel;
    }
}
