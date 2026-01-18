package com.campaignworkbench.ide.editor;

import com.campaignworkbench.ide.IDETheme;
import com.campaignworkbench.ide.ThemeManager;
import javafx.application.Platform;
import javafx.embed.swing.SwingNode;
import javafx.scene.Node;

import org.fife.ui.rsyntaxtextarea.*;
import org.fife.ui.rtextarea.RTextScrollPane;

import javax.swing.*;
import java.awt.event.HierarchyEvent;
import java.io.IOException;

public class RSyntaxEditor implements ICodeEditor {

    private final SwingNode swingNode = new SwingNode();
    private RSyntaxTextArea textArea;

    private IDETheme pendingTheme;

    public RSyntaxEditor() {
        Platform.runLater(() -> {
            initSwing();
            ThemeManager.register(this);
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

    public void applyThemeAsync(Theme theme) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                if (textArea.getGraphics() != null) {
                    // Safe to apply theme now
                    theme.apply(textArea);
                } else {
                    // Not ready yet, try again on the next repaint
                    Timer timer = new Timer(50, e -> {
                        if (textArea.getGraphics() != null) {
                            theme.apply(textArea);
                            ((Timer) e.getSource()).stop();
                        }
                    });
                    timer.setRepeats(true);
                    timer.start();
                }
            }
        });
    }

    public void applyTheme(IDETheme ideTheme) {
        try {
            Theme themeToApply;

            switch (ideTheme) {
                case DARK:
                    themeToApply = Theme.load(getClass().getResourceAsStream(
                            "/rsyntaxtextarea/themes/dark.xml"));
                    break;
                default:
                    themeToApply = Theme.load(getClass().getResourceAsStream(
                            "/rsyntaxtextarea/themes/default.xml"));
                    break;
            }
            applyThemeAsync(themeToApply);
        }  catch (IOException ioe) { // Never happens
            ioe.printStackTrace();
        }
    }
}
