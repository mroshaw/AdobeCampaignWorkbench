package com.campaignworkbench.ide;

import com.campaignworkbench.ide.editor.ICodeEditor;
import com.campaignworkbench.ide.editor.richtextfx.RichTextFXEditor;
import com.campaignworkbench.ide.editor.SyntaxType;
import javafx.scene.control.Tab;

public class OutputTab extends Tab   {
    private final ICodeEditor editor;

    public OutputTab(String title, SyntaxType syntaxType) {
        setText(title);
        this.editor = new RichTextFXEditor(syntaxType);
        // this.editor = new RSyntaxEditor();
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

    public void gotoLine(int lineNumber) {
        editor.gotoLine(lineNumber);
    }
}
