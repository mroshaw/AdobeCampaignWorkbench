package com.campaignworkbench.ide.editor;

import com.campaignworkbench.ide.IThemeable;
import com.campaignworkbench.ide.IDETheme;
import com.campaignworkbench.ide.ThemeManager;
import javafx.application.Platform;
import javafx.embed.swing.SwingNode;
import javafx.scene.Node;

import org.fife.ui.rsyntaxtextarea.*;
import org.fife.ui.rtextarea.RTextScrollPane;

import java.awt.*;
import java.io.IOException;

public class RSyntaxEditor implements ICodeEditor, IThemeable {

    private final SwingNode swingNode = new SwingNode();
    private RSyntaxTextArea textArea;

    private IDETheme pendingTheme;


    public RSyntaxEditor() {
        Platform.runLater(() -> {
            initSwing();               // create the RSyntaxTextArea
            ThemeManager.register(this); // now register it and apply current theme
        });
    }

    private void initSwing() {
        textArea = new RSyntaxTextArea(25, 80);
        textArea.setAntiAliasingEnabled(true);
        textArea.setCodeFoldingEnabled(true);
        textArea.setBracketMatchingEnabled(true);
        textArea.setAutoIndentEnabled(true);
        textArea.setMarkOccurrences(true);
        textArea.setClearWhitespaceLinesEnabled(false);
        textArea.setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.TEXT_CURSOR));
        // textArea.setFont(new java.awt.Font("JetBrains Mono", java.awt.Font.PLAIN, 11));
        // textArea.setFont(new java.awt.Font("Fira Code", java.awt.Font.PLAIN, 11));
        // textArea.setFont(new java.awt.Font("Consolas", java.awt.Font.PLAIN, 11));
        textArea.setFont(new java.awt.Font("Source Code Pro", java.awt.Font.PLAIN, 14));

        RTextScrollPane scrollPane = new RTextScrollPane(textArea);
        scrollPane.setFoldIndicatorEnabled(true);

        swingNode.setContent(scrollPane);
        swingNode.setCursor(javafx.scene.Cursor.TEXT);
    }

    @Override
    public Node getNode() {
        return swingNode;
    }

    @Override
    public void setText(String text) {
        Platform.runLater(() -> textArea.setText(text));
    }

    @Override
    public String getText() {
        return textArea.getText();
    }

    @Override
    public void setEditable(boolean editable) {
        Platform.runLater(() -> textArea.setEditable(editable));
    }

    @Override
    public void requestFocus() {
        Platform.runLater(() -> textArea.requestFocusInWindow());
    }

    @Override
    public void setSyntax(SyntaxType syntax) {
        Platform.runLater(() -> {
            switch (syntax) {
                case TEMPLATE:
                    textArea.setSyntaxEditingStyle(
                            SyntaxConstants.SYNTAX_STYLE_JSP
                    );
                    break;

                case BLOCK:
                case SOURCE_PREVIEW:
                    textArea.setSyntaxEditingStyle(
                            SyntaxConstants.SYNTAX_STYLE_JAVASCRIPT
                    );
                    break;

                case XML:
                    textArea.setSyntaxEditingStyle(
                            SyntaxConstants.SYNTAX_STYLE_XML
                    );
                    break;
                case PLAIN:
                default:
                    textArea.setSyntaxEditingStyle(
                            SyntaxConstants.SYNTAX_STYLE_NONE
                    );
                    break;
            }
        });
    }


    @Override
    public void applyTheme(IDETheme ideTheme) {
        try {
            Theme themeToApply;

            switch (ideTheme) {
                case LIGHT:
                    themeToApply = Theme.load(getClass().getResourceAsStream(
                            "/rsyntaxtextarea/themes/default.xml"));
                    break;
                case DARK:
                    themeToApply = Theme.load(getClass().getResourceAsStream(
                            "/rsyntaxtextarea/themes/dark.xml"));
                    break;
                default:
                    themeToApply = Theme.load(getClass().getResourceAsStream(
                            "/rsyntaxtextarea/themes/default.xml"));
                    break;
            }
            themeToApply.apply(textArea);

        } catch(NullPointerException npe) {

        } catch (IOException ioe) { // Never happens
            ioe.printStackTrace();
        }
    }
}
