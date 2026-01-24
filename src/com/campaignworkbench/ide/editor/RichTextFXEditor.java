package com.campaignworkbench.ide.editor;

import javafx.scene.Node;

/**
 * Placeholder implementation of ICodeEditor using RichTextFX (currently not implemented)
 */
public class RichTextFXEditor implements ICodeEditor {

    @Override
    public Node getNode() {
        return null;
    }

    @Override
    public void refreshContent() {

    }

    @Override
    public String getText() {
        return "";
    }

    @Override
    public void setSyntax(SyntaxType syntax) {

    }

    @Override
    public void setEditable(boolean editable) {

    }

    @Override
    public void requestFocus() {

    }

    @Override
    public void setCaretAtStart() {

    }

    @Override
    public void gotoLine(int line) {

    }
}
