package com.campaignworkbench.ide;

import com.campaignworkbench.ide.editor.ICodeEditor;
import com.campaignworkbench.ide.editor.SyntaxType;
import com.campaignworkbench.ide.editor.richtextfx.RichTextFXEditor;
import javafx.scene.control.Tab;

/**
 * Class representing a tab in the output panel.
 */
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
