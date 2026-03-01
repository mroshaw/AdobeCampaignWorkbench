package com.campaignworkbench.ide.editor.richtextfx;

import com.campaignworkbench.ide.IdeException;
import com.campaignworkbench.ide.IDETheme;
import com.campaignworkbench.ide.IThemeable;
import com.campaignworkbench.ide.ThemeManager;
import com.campaignworkbench.ide.editor.ICodeEditor;
import com.campaignworkbench.ide.editor.SyntaxType;
import javafx.application.Platform;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.IndexRange;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import org.fxmisc.flowless.VirtualizedScrollPane;
import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.model.StyleSpans;
import org.fxmisc.richtext.model.TwoDimensional;
import org.reactfx.Subscription;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Implementation of ICodeEditor using the RichTextFX library
 */
public class RichTextFXEditor implements ICodeEditor, IThemeable {

    private final CodeArea codeArea;
    private final BorderPane root;

    // Syntax highlighting
    private ISyntaxStyler syntaxStyler;

    // Code Folding
    private IFoldParser foldParser;

    // Code formatting
    private ICodeFormatter codeFormatter;

    /**
     * Constructor
     */
    public RichTextFXEditor(SyntaxType syntaxType) {
        codeArea = new CodeArea();
        codeArea.setCursor(Cursor.TEXT);

        VirtualizedScrollPane<CodeArea> scrollPane = new VirtualizedScrollPane<>(codeArea);
        root = new BorderPane(scrollPane);
        root.getStyleClass().add("code-editor");

        setLanguageHelpers(syntaxType);

        GutterFactory gutterFactory = new GutterFactory(codeArea, foldParser);
        codeArea.setParagraphGraphicFactory(gutterFactory);


        codeArea.textProperty().addListener((_, _, newText) -> {
            StyleSpans<Collection<String>> computedStyleSpans = syntaxStyler.style(newText);
            if (computedStyleSpans != null) {
                codeArea.setStyleSpans(0, computedStyleSpans);
            }
        });

        codeArea.addEventFilter(KeyEvent.KEY_PRESSED, e -> {
            if (e.getCode() == KeyCode.TAB) {
                e.consume();

                IndexRange selection = codeArea.getSelection();

                // No selection → insert two spaces at caret
                if (selection.getLength() == 0) {
                    codeArea.replaceSelection("  ");
                    return;
                }

                int startPar = codeArea.offsetToPosition(selection.getStart(), TwoDimensional.Bias.Forward).getMajor();
                int endPar   = codeArea.offsetToPosition(selection.getEnd(), TwoDimensional.Bias.Backward).getMajor();

                var multi = codeArea.createMultiChange();

                    for (int i = startPar; i <= endPar; i++) {
                        int lineStart = codeArea.getAbsolutePosition(i, 0);
                        multi.insertText(lineStart, "  ");
                    }

                multi.commit();

                // Restore selection expanded by 2 chars per line
                int lines = endPar - startPar + 1;
                codeArea.selectRange(
                        selection.getStart(),
                        selection.getEnd() + (lines * 2)
                );
            }
        });

        Platform.runLater(() -> ThemeManager.register(this));
    }

    @Override
    public Node getNode() {
        return root;
    }

    @Override
    public void refreshContent() {
        // RichTextFX usually handles its own repainting
    }

    @Override
    public void setText(String text) {
        codeArea.clear();
        // Replace tab with 2 spaces
        codeArea.replaceText(text.replaceAll("\t", "  "));
        StyleSpans<Collection<String>> computedStyleSpans = syntaxStyler.style(codeArea.getText());
        if (computedStyleSpans != null) {
            codeArea.setStyleSpans(0, computedStyleSpans);
        }
    }

    @Override
    public String getText() {
        return codeArea.getText();
    }

    private void setLanguageHelpers(SyntaxType syntax) {
        // Set the folding class instance
        if (Objects.requireNonNull(syntax) == SyntaxType.XML) {
            codeFormatter = new XmlFormatter();
            syntaxStyler = new XmlStyler();
            foldParser = new XmlFoldParser(codeArea);
        } else {
            codeFormatter = null;
            syntaxStyler = new CampaignStyler();
            foldParser = new CampaignFoldParser(codeArea);
        }
    }

    @Override
    public void setEditable(boolean editable) {
        codeArea.setEditable(editable);
    }

    @Override
    public void requestFocus() {
        codeArea.requestFocus();
    }

    @Override
    public void setCaretAtStart() {
        codeArea.moveTo(0);
        codeArea.requestFollowCaret();
    }

    @Override
    public void gotoLine(int line) {
        if (line <= 0) return;
        int paragraphIndex = Math.min(line - 1, codeArea.getParagraphs().size() - 1);
        codeArea.moveTo(paragraphIndex, 0);
        codeArea.selectLine();
        codeArea.requestFollowCaret();
    }

    @Override
    public void insertTextAtCaret(String text) {
        if (text == null || text.isEmpty()) {
            return;
        }

        int caretPos = codeArea.getCaretPosition();

        // If there's a selection, replace it
        if (codeArea.getSelection().getLength() > 0) {
            codeArea.replaceText(codeArea.getSelection(), text);
        } else {
            codeArea.insertText(caretPos, text);
        }

        // Move caret to after inserted text
        codeArea.moveTo(caretPos + text.length());
    }

    @Override
    public void applyTheme(IDETheme theme) {
        root.getStylesheets().clear();

        if (syntaxStyler != null) {
            String languageCss = syntaxStyler.getStyleSheet(theme);
            root.getStylesheets().add(languageCss);
        }

    }

    public void formatCode(int indentSize) {
        if (codeFormatter == null) {
            return;
        }
        try {
            String formattedCode = codeFormatter.format(getText(), indentSize);
            setText(formattedCode);
        } catch (Exception ex) {
            throw new IdeException("Error formatting code", ex);
        }
    }

    @Override
    public void foldAll() {
        foldParser.foldAll();
    }

    @Override
    public void unfoldAll() {
        foldParser.unfoldAll();
    }

    @Override
    public Subscription addTextChangeListener(Consumer<String> listener) {
        return codeArea.multiPlainChanges()
                .subscribe(change -> listener.accept(codeArea.getText()));
    }

    @Override
    public void find(String text) {
        if (text == null || text.isEmpty()){
            clearFindHighlight();
            return;
        }

        String content = codeArea.getText();
        Pattern pattern = Pattern.compile(Pattern.quote(text), Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(content);

        while (matcher.find()) {
            int start = matcher.start();
            int end = matcher.end();
            for (int i = start; i < end; i++) {
                codeArea.setStyle(i, i + 1, mergeStyles(codeArea.getStyleOfChar(i)));
            }
        }
    }

    public void clearFindHighlight() {

        int length = codeArea.getLength();

        for (int i = 0; i < length; i++) {

            Collection<String> styles = codeArea.getStyleOfChar(i);

            if (styles.contains("find-text")) {

                List<String> cleaned =
                        styles.stream()
                                .filter(s -> !s.equals("find-text"))
                                .toList();

                codeArea.setStyle(i, i + 1, cleaned);
            }
        }
    }

    /**
     * Returns a new style collection containing the original styles plus the highlight
     */
    private Collection<String> mergeStyles(Collection<String> original) {
        if (original.contains("find-text")) return original; // already highlighted
        ArrayList<String> merged = new ArrayList<>(original);
        merged.add("find-text");
        return merged;
    }
}
