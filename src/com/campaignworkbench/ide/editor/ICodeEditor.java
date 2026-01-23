package com.campaignworkbench.ide.editor;

import javafx.scene.Node;

/**
 * Interface describing a component for editing code
 */
public interface ICodeEditor {

    /**
     * @return the JavaFX node
     */
    public Node getNode();

    /**
     * Refresh the code content of the editor
     */
    public void refreshContent();

    /**
     * Set the  code content of the editor
     * @param text the content of the editor
     */
    default void setText(String text) {

    }

    /**
     * Get the code context of the editor
     * @return the text content of the editor
     */
    public String getText();

    /**
     * Set the syntax highlighting required by the editor
     * @param syntax Language syntax to use for highlighting
     */
    public void setSyntax(SyntaxType syntax);

    /**
     * Sets the read only nature of the code editor
     * @param editable whether the code is editable (true) or not (false)
     */
    public void setEditable(boolean editable);

    /**
     * Requests UI focus of the control
     */
    public void requestFocus();

    public void setCaretAtStart();

}
