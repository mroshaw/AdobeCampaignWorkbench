package com.campaignworkbench.ide.editor;

import javafx.application.Platform;
import javafx.embed.swing.SwingNode;
import javafx.scene.Node;

import org.fife.ui.rsyntaxtextarea.*;
import org.fife.ui.rtextarea.RTextScrollPane;

public final class RSyntaxEditor implements CodeEditor {

    private final SwingNode swingNode = new SwingNode();
    private RSyntaxTextArea textArea;

    public RSyntaxEditor() {
        Platform.runLater(this::initSwing);
    }

    private void initSwing() {
        textArea = new RSyntaxTextArea(25, 80);
        textArea.setAntiAliasingEnabled(true);
        textArea.setCodeFoldingEnabled(true);
        textArea.setBracketMatchingEnabled(true);
        textArea.setAutoIndentEnabled(true);
        textArea.setMarkOccurrences(true);
        textArea.setClearWhitespaceLinesEnabled(false);

        RTextScrollPane scrollPane = new RTextScrollPane(textArea);
        scrollPane.setFoldIndicatorEnabled(true);

        swingNode.setContent(scrollPane);
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

}
