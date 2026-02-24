package com.campaignworkbench.ide.editor;

import javafx.scene.Node;
import org.reactfx.Subscription;

import java.util.function.Consumer;

/**
 * Interface describing a component for editing code
 */
public interface ICodeEditor {

    /**
     * @return the JavaFX node
     */
    Node getNode();

    /**
     * Refresh the code content of the editor
     */
    void refreshContent();

    /**
     * Set the code content of the editor
     * @param text the content of the editor
     */
    void setText(String text);

    /**
     * Get the code context of the editor
     * @return the text content of the editor
     */
    String getText();

    /**
     * Sets the read-only nature of the code editor
     * @param editable whether the code is editable (true) or not (false)
     */
    void setEditable(boolean editable);

    /**
     * Requests UI focus of the control
     */
    void requestFocus();

    /**
     * Sets the caret position at the start of the document
     */
    void setCaretAtStart();

    /**
     * Moves the caret to the specified line number and selects it
     * @param line the line number to move the caret to
     */
    void gotoLine(int line);

    void find(String textToFind);

    void formatCode(int indentSize);

    void foldAll();

    void unfoldAll();

    Subscription addTextChangeListener(Consumer<String> listener);

}
