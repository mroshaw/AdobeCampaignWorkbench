package com.campaignworkbench.ide.editor;

import com.campaignworkbench.ide.IDETheme;
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
    public void setText(String text);

    /**
     * Get the code context of the editor
     * @return the text content of the editor
     */
    public String getText();

    /**
     * Sets the read only nature of the code editor
     * @param editable whether the code is editable (true) or not (false)
     */
    public void setEditable(boolean editable);

    /**
     * Requests UI focus of the control
     */
    public void requestFocus();

    /**
     * Sets the caret position at the start of the document
     */
    public void setCaretAtStart();

    /**
     * Moves the caret to the specified line number and selects it
     * @param line the line number to move the caret to
     */
    public void gotoLine(int line);

    public void openFindDialog(String fileName);

}
