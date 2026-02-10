package com.campaignworkbench.ide;

import com.campaignworkbench.ide.editor.ICodeEditor;
import com.campaignworkbench.ide.editor.RSyntaxEditor;
import com.campaignworkbench.ide.editor.SyntaxType;
import javafx.scene.control.Tab;

public class OutputTab extends Tab   {
    private final ICodeEditor editor;

    public OutputTab(String title, SyntaxType syntaxType) {
        setText(title);
        this.editor = new RSyntaxEditor();
        editor.setSyntax(syntaxType);
        setContent(editor.getNode());
        setClosable(false);
    }

    public void setContentText(String content) {
        editor.setText(content);
        editor.setCaretAtStart();
    }

    public void refreshContent() {
        System.out.println("Refreshing...");
        editor.refreshContent();
    }
}
