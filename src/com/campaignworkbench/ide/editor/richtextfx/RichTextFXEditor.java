package com.campaignworkbench.ide.editor.richtextfx;

import com.campaignworkbench.ide.IDEException;
import com.campaignworkbench.ide.IDETheme;
import com.campaignworkbench.ide.IThemeable;
import com.campaignworkbench.ide.ThemeManager;
import com.campaignworkbench.ide.editor.ICodeEditor;
import com.campaignworkbench.ide.editor.SyntaxType;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import org.fxmisc.richtext.CodeArea;
import org.fxmisc.flowless.VirtualizedScrollPane;
import javafx.scene.Cursor;
import org.fxmisc.richtext.model.Paragraph;
import org.fxmisc.richtext.model.StyleSpans;
import org.fxmisc.richtext.model.StyleSpansBuilder;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Implementation of ICodeEditor using the RichTextFX library
 */
public class RichTextFXEditor implements ICodeEditor, IThemeable {

    private final CodeArea codeArea;
    private final VirtualizedScrollPane<CodeArea> scrollPane;
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

        scrollPane = new VirtualizedScrollPane<>(codeArea);
        root = new BorderPane(scrollPane);
        root.getStyleClass().add("code-editor");

        setLanguageHelpers(syntaxType);

        GutterFactory gutterFactory = new GutterFactory(codeArea, foldParser);
        codeArea.setParagraphGraphicFactory(gutterFactory);
        Platform.runLater(() -> ThemeManager.register(this));

        codeArea.textProperty().addListener((obs, oldText, newText) -> {
            StyleSpans<Collection<String>> computedStyleSpans = syntaxStyler.style(newText);
            if (computedStyleSpans != null) {
                codeArea.setStyleSpans(0, computedStyleSpans);
            }
        });
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
        codeArea.replaceText(text);
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
        // Set folding class instance
        switch (syntax) {
            case XML:
                codeFormatter = new XmlFormatter();
                syntaxStyler = new XmlStyler();
                foldParser = new XmlFoldParser(codeArea);
                break;
            default:
                codeFormatter = null;
                syntaxStyler = new CampaignStyler();
                foldParser = new CampaignFoldParser(codeArea);
                break;
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
    public void applyTheme(IDETheme theme) {

        if (theme == IDETheme.DARK) {
            if (syntaxStyler != null) {
                // String codeEditorCss = theme.getCodeEditorStyleSheet();
                // root.getScene().getStylesheets().add(codeEditorCss);
                String languageCss = syntaxStyler.getStyleSheet(theme);
                // root.getScene().getStylesheets().add(languageCss);
                // root.getStylesheets().add(codeEditorCss);
                root.getStylesheets().add(languageCss);
            }
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
            throw new IDEException("Error formatting code", ex);
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
    public void find(String text) {
        if (text == null || text.isEmpty()) return;

        String content = codeArea.getText();
        Pattern pattern = Pattern.compile(Pattern.quote(text), Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(content);

        while (matcher.find()) {
            int start = matcher.start();
            int end = matcher.end();
            for (int i = start; i < end; i++) {
                codeArea.setStyle(i, i + 1, mergeStyles(codeArea.getStyleOfChar(i), "find-text"));
            }
        }
    }
    /**
     * Returns a new style collection containing the original styles plus the highlight
     */
    private Collection<String> mergeStyles(Collection<String> original, String newStyle) {
        if (original.contains(newStyle)) return original; // already highlighted
        ArrayList<String> merged = new ArrayList<>(original);
        merged.add(newStyle);
        return merged;
    }
}
