package com.campaignworkbench.ide.editor.rsyntaxtextarea;

import com.campaignworkbench.ide.IDETheme;
import com.campaignworkbench.ide.IThemeable;
import com.campaignworkbench.ide.ThemeManager;
import com.campaignworkbench.ide.editor.ICodeEditor;
import com.campaignworkbench.ide.editor.SyntaxType;
import javafx.application.Platform;
import javafx.embed.swing.SwingNode;
import javafx.scene.Node;

import javafx.scene.Parent;
import javafx.stage.Stage;
import org.fife.rsta.ac.LanguageSupportFactory;
import org.fife.ui.rsyntaxtextarea.*;
import org.fife.ui.rtextarea.RTextScrollPane;

import javax.swing.*;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Queue;

/**
 * Provided an implementation of the ICodeEditor interface using the RSyntaxTextArea control
 */
public class RSyntaxEditor implements ICodeEditor, IThemeable {

    private final SwingNode swingNode = new SwingNode();
    /**
     * The underlying RSyntaxTextArea component
     */
    private RSyntaxTextArea rSyntaxTextArea;

    private RSyntaxFindDialog findDialog;
    RTextScrollPane scrollPane;

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
            // rSyntaxTextArea.setLineWrap(true);
            LanguageSupportFactory.get().register(rSyntaxTextArea);

             scrollPane = new RTextScrollPane(rSyntaxTextArea);
            scrollPane.setFoldIndicatorEnabled(true);

            findDialog = new RSyntaxFindDialog(rSyntaxTextArea, (Stage) getNode().getScene().getWindow());

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
        Platform.runLater(() -> {
            Parent parent = (Parent) swingNode.getParent();
            if (parent != null) {
                parent.requestLayout();   // public API
            }

            SwingUtilities.invokeLater(() -> {
                rSyntaxTextArea.revalidate();
                rSyntaxTextArea.repaint();
            });
        });

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

    /**
     * Moves the caret to the specified line
     * @param line The line number to go to (1-indexed)
     */
    @Override
    public void gotoLine(int line) {
        if (line <= 0) return;
        runOrQueue(() -> {
            try {

                int scrollBarY = rSyntaxTextArea.yForLine(line - 5);
                scrollPane.getVerticalScrollBar().setValue(scrollBarY);


                int lineStartOffset = rSyntaxTextArea.getLineStartOffset(line - 1);
                int lineEndOffset = rSyntaxTextArea.getLineEndOffset(line - 1);
                rSyntaxTextArea.setCaretPosition(lineStartOffset);
                rSyntaxTextArea.moveCaretPosition(lineEndOffset);
                // rSyntaxTextArea.requestFocusInWindow();

            } catch (Exception e) {
                // Ignore invalid line numbers
            }
        });
    }

    public void setSyntax(SyntaxType syntax) {
        runOrQueue(() -> {
            String style = switch (syntax) {
                case CAMPAIGN -> SyntaxConstants.SYNTAX_STYLE_JSP;
                case JAVASCRIPT -> SyntaxConstants.SYNTAX_STYLE_JAVASCRIPT;
                case XML -> SyntaxConstants.SYNTAX_STYLE_XML;
                case HTML -> SyntaxConstants.SYNTAX_STYLE_HTML;
                default -> SyntaxConstants.SYNTAX_STYLE_NONE;
            };
            rSyntaxTextArea.setSyntaxEditingStyle(style);
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

    public void applyTheme(IDETheme ideTheme) {
        try {
            String themePath = switch (ideTheme) {
                case DARK -> "/rsyntaxtextarea/themes/monokai.xml";
                default -> "/rsyntaxtextarea/themes/default.xml";
            };
            Theme themeToApply = Theme.load(getClass().getResourceAsStream(themePath));
            applyThemeAsync(themeToApply);
        } catch (IOException ioe) { // Never happens
            ioe.printStackTrace();
        }
    }

    @Override
    public void openFindDialog(String fileName) {
        findDialog.show(fileName);
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
