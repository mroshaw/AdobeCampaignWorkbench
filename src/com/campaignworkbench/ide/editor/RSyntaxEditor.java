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
import java.util.ArrayDeque;
import java.util.Queue;

/**
 * Provided an implementation of the ICodeEditor interface using the RSyntaxTextArea control
 */
public class RSyntaxEditor implements ICodeEditor {

    private final SwingNode swingNode = new SwingNode();
    private RSyntaxTextArea rSyntaxTextArea;

    // Queue all actions until RSyntaxTextArea is ready
    private final Queue<Runnable> pendingActions = new ArrayDeque<>();

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
        runOnEDT(() -> {
            rSyntaxTextArea = new RSyntaxTextArea(25, 80);
            rSyntaxTextArea.setAntiAliasingEnabled(true);
            rSyntaxTextArea.setCodeFoldingEnabled(true);
            rSyntaxTextArea.setBracketMatchingEnabled(true);
            rSyntaxTextArea.setAutoIndentEnabled(true);
            rSyntaxTextArea.setMarkOccurrences(true);
            rSyntaxTextArea.setClearWhitespaceLinesEnabled(false);
            rSyntaxTextArea.setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.TEXT_CURSOR));

            RTextScrollPane scrollPane = new RTextScrollPane(rSyntaxTextArea);
            scrollPane.setFoldIndicatorEnabled(true);

            swingNode.setContent(scrollPane);
            swingNode.setCursor(javafx.scene.Cursor.TEXT);

            // Run any queued actions safely now that the component exists
            while (!pendingActions.isEmpty()) {
                pendingActions.poll().run();
            }
        });
    }

    @Override
    public Node getNode() {
        return swingNode;
    }

    @Override
    public void refreshContent() {
        runOrQueue(this::validateAndRepaint);
    }

    private void validateAndRepaint() {
        rSyntaxTextArea.revalidate();
        rSyntaxTextArea.repaint();
    }

    @Override
    public void setText(String text) {
        runOrQueue(() -> rSyntaxTextArea.setText(text));
    }

    @Override
    public String getText() {
        return rSyntaxTextArea != null ? rSyntaxTextArea.getText() : "";
    }

    @Override
    public void setEditable(boolean editable) {
        runOrQueue(() -> rSyntaxTextArea.setEditable(editable));
    }

    @Override
    public void requestFocus() {
        runOrQueue(() -> rSyntaxTextArea.requestFocusInWindow());
    }

    @Override
    public void setCaretAtStart() {
        runOrQueue(() -> {
            rSyntaxTextArea.setCaretPosition(0);
        });
    }

    @Override
    public void gotoLine(int line) {
        if (line <= 0) return;
        runOrQueue(() -> {
            try {
                int lineStartOffset = rSyntaxTextArea.getLineStartOffset(line - 1);
                rSyntaxTextArea.setCaretPosition(lineStartOffset);
                rSyntaxTextArea.requestFocusInWindow();
            } catch (Exception e) {
                // Ignore invalid line numbers
            }
        });
    }

    @Override
    public void setSyntax(SyntaxType syntax) {
        runOrQueue(() -> {
            switch (syntax) {
                case TEMPLATE:
                    rSyntaxTextArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_JSP);
                    break;
                case BLOCK:
                case SOURCE_PREVIEW:
                    rSyntaxTextArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_JAVASCRIPT);
                    break;
                case XML:
                    rSyntaxTextArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_XML);
                    break;
                case HTML_PREVIEW:
                    rSyntaxTextArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_HTML);
                    break;
                case PLAIN:
                default:
                    rSyntaxTextArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_NONE);
                    break;
            }
        });
    }

    private void applyThemeAsync(Theme theme) {
        runOrQueue(() -> {
            // Safely defer Theme.apply() until graphics are ready
            if (rSyntaxTextArea.getGraphics() != null) {
                theme.apply(rSyntaxTextArea);
            } else {
                // Component exists but not painted yet; retry once on next EDT tick
                SwingUtilities.invokeLater(() -> applyThemeAsync(theme));
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
        } catch (IOException ioe) { // Never happens
            ioe.printStackTrace();
        }
    }

    /**
     * Helper to safely run code on the Swing EDT, or queue if the component isn't created yet
     */
    private void runOrQueue(Runnable action) {
        if (rSyntaxTextArea == null) {
            pendingActions.add(() -> runOnEDT(action));
        } else {
            runOnEDT(action);
        }
    }

    /**
     * Helper to safely run code on the Swing EDT
     */
    private void runOnEDT(Runnable action) {
        if (SwingUtilities.isEventDispatchThread()) {
            action.run();
        } else {
            SwingUtilities.invokeLater(action);
        }
    }
}
