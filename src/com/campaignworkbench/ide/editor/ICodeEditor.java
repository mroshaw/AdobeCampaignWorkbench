package com.campaignworkbench.ide.editor;

import javafx.scene.Node;

public interface ICodeEditor {

    Node getNode();

    void setText(String text);
    String getText();

    void setSyntax(SyntaxType syntax);

    void setEditable(boolean editable);

    void requestFocus();

}
