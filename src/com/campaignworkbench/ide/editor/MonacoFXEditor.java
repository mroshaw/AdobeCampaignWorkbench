package com.campaignworkbench.ide.editor;

import com.campaignworkbench.ide.IDETheme;
import com.campaignworkbench.ide.ThemeManager;
import eu.mihosoft.monacofx.MonacoFX;
import javafx.scene.Node;
import javafx.scene.web.WebEngine;

public class MonacoFXEditor implements ICodeEditor {
    MonacoFX monacoFX;

    public MonacoFXEditor() {
        monacoFX = new MonacoFX();
        ThemeManager.register(this);
        // use a predefined language like 'c'
        // monacoFX.getEditor().setCurrentLanguage("HTML");
        // monacoFX.getEditor().setCurrentTheme("vs-dark");
    }

    @Override
    public Node getNode() {
        return monacoFX;
    }

    @Override
    public void refreshContent() {

    }

    @Override
    public void setText(String text) {
        monacoFX.getEditor().getDocument().setText(text);
    }

    @Override
    public String getText() {
        return monacoFX.getEditor().getDocument().getText();
    }

    @Override
    public void setSyntax(SyntaxType syntax) {
        switch(syntax)
        {
            case XML:
                monacoFX.getEditor().setCurrentLanguage("xml");
                break;
            case HTML_PREVIEW:
                monacoFX.getEditor().setCurrentLanguage("html");
                break;
            case TEMPLATE:
                monacoFX.getEditor().setCurrentLanguage("html");
                break;
            case SOURCE_PREVIEW:
                monacoFX.getEditor().setCurrentLanguage("javascript");
                break;
            case BLOCK:
                monacoFX.getEditor().setCurrentLanguage("html");
                break;

            case MODULE:
                monacoFX.getEditor().setCurrentLanguage("JSP");
            default:
                break;
        }
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
        // monacoFX.getEditor().getDocument().setSelections
    }

    @Override
    public void applyTheme(IDETheme theme) {
        switch(theme) {
            case LIGHT:
                monacoFX.getEditor().setCurrentTheme("vs-light");
                break;

            case DARK:
            default:
                monacoFX.getEditor().setCurrentTheme("vs-dark");
                break;

        }
    }
}
