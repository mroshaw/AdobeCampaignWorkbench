package com.campaignworkbench.ide.editor;

import com.campaignworkbench.ide.IDETheme;
import com.campaignworkbench.ide.ThemeManager;
import javafx.application.Platform;
import javafx.embed.swing.SwingNode;
import javafx.scene.Node;

import org.fife.ui.rsyntaxtextarea.*;
import org.fife.ui.rtextarea.RTextScrollPane;

import javax.swing.*;
import java.io.IOException;

/**
 * Provided an implementation of the ICodeEditor interface using the RSyntaxTextArea control
 */
public class RSyntaxEditor implements ICodeEditor {

    private final SwingNode swingNode = new SwingNode();
    private RSyntaxTextArea rSyntaxTextArea;

    private IDETheme pendingTheme;

    /**
     * Constructor
     */
    public RSyntaxEditor() {
        Platform.runLater(() -> {
            initSwing();
            ThemeManager.register(this);
        });
    }

    /**
     * Initialise the swing components
     */
    private void initSwing() {
        rSyntaxTextArea = new RSyntaxTextArea(25, 80);
        rSyntaxTextArea.setAntiAliasingEnabled(true);
        rSyntaxTextArea.setCodeFoldingEnabled(true);
        rSyntaxTextArea.setBracketMatchingEnabled(true);
        rSyntaxTextArea.setAutoIndentEnabled(true);
        rSyntaxTextArea.setMarkOccurrences(true);
        rSyntaxTextArea.setClearWhitespaceLinesEnabled(false);
        rSyntaxTextArea.setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.TEXT_CURSOR));
        // textArea.setFont(new java.awt.Font("JetBrains Mono", java.awt.Font.PLAIN, 11));
        // textArea.setFont(new java.awt.Font("Fira Code", java.awt.Font.PLAIN, 11));
        // textArea.setFont(new java.awt.Font("Consolas", java.awt.Font.PLAIN, 11));

        RTextScrollPane scrollPane = new RTextScrollPane(rSyntaxTextArea);
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
        Platform.runLater(() -> rSyntaxTextArea.setText(text));
    }

    @Override
    public String getText() {
        return rSyntaxTextArea.getText();
    }

    @Override
    public void setEditable(boolean editable) {
        Platform.runLater(() -> rSyntaxTextArea.setEditable(editable));
    }

    @Override
    public void requestFocus() {
        Platform.runLater(() -> rSyntaxTextArea.requestFocusInWindow());
    }

    @Override
    public void setSyntax(SyntaxType syntax) {
        Platform.runLater(() -> {
            switch (syntax) {
                case TEMPLATE:
                    rSyntaxTextArea.setSyntaxEditingStyle(
                            SyntaxConstants.SYNTAX_STYLE_JSP
                    );
                    break;

                case BLOCK:
                case SOURCE_PREVIEW:
                    rSyntaxTextArea.setSyntaxEditingStyle(
                            SyntaxConstants.SYNTAX_STYLE_JAVASCRIPT
                    );
                    break;

                case XML:
                    rSyntaxTextArea.setSyntaxEditingStyle(
                            SyntaxConstants.SYNTAX_STYLE_XML
                    );
                    break;
                case PLAIN:
                default:
                    rSyntaxTextArea.setSyntaxEditingStyle(
                            SyntaxConstants.SYNTAX_STYLE_NONE
                    );
                    break;
            }
        });
    }

    private void applyThemeAsync(Theme theme) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                if (rSyntaxTextArea.getGraphics() != null) {
                    // Safe to apply theme now
                    theme.apply(rSyntaxTextArea);
                } else {
                    // Not ready yet, try again on the next repaint
                    Timer timer = new Timer(50, e -> {
                        if (rSyntaxTextArea.getGraphics() != null) {
                            theme.apply(rSyntaxTextArea);
                            ((Timer) e.getSource()).stop();
                        }
                    });
                    timer.setRepeats(true);
                    timer.start();
                }
            }
        });
    }

    /**
     * Applies a light or dark theme to all controls in the UI
     * @param ideTheme enum of the LIGHT or DARK theme
     */
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
