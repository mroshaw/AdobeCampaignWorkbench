package com.campaignworkbench.ide.editor;

import javafx.application.Platform;
import javafx.embed.swing.SwingNode;
import javafx.scene.Node;

import org.fife.ui.rsyntaxtextarea.*;
import org.fife.ui.rtextarea.RTextScrollPane;

import java.io.IOException;
import java.io.InputStream;

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
    public void applyDarkTheme(boolean isDark) {
        Platform.runLater(() -> {
            if (isDark) {
                try (InputStream themeStream = getClass().getResourceAsStream(
                        "/rsyntaxtextarea/themes/dark.xml")) {
                    if (themeStream != null) {
                        org.fife.ui.rsyntaxtextarea.Theme theme = org.fife.ui.rsyntaxtextarea.Theme.load(themeStream);
                        theme.apply(textArea);
                    } else {
                        System.err.println("Dark theme not found on classpath!");
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                // Optional: Reset to default theme (light)
                textArea.setBackground(java.awt.Color.WHITE);
                textArea.setForeground(java.awt.Color.BLACK);
            }
        });
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



}
