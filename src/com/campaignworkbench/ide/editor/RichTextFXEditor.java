package com.campaignworkbench.ide.editor;

import com.campaignworkbench.ide.IDETheme;
import com.campaignworkbench.ide.ThemeManager;
import javafx.application.Platform;
import javafx.scene.Node;
import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.LineNumberFactory;
import org.fxmisc.richtext.model.StyleSpans;
import org.fxmisc.richtext.model.StyleSpansBuilder;
import org.fxmisc.flowless.VirtualizedScrollPane;

import java.util.Collection;
import java.util.Collections;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Implementation of ICodeEditor using the RichTextFX library
 */
public class RichTextFXEditor implements ICodeEditor {

    private final CodeArea codeArea;
    private final VirtualizedScrollPane<CodeArea> scrollPane;
    private SyntaxType currentSyntax = SyntaxType.PLAIN;

    private static final String[] KEYWORDS = new String[] {
            "abstract", "assert", "boolean", "break", "byte",
            "case", "catch", "char", "class", "const",
            "continue", "default", "do", "double", "else",
            "enum", "extends", "final", "finally", "float",
            "for", "goto", "if", "implements", "import",
            "instanceof", "int", "interface", "long", "native",
            "new", "package", "private", "protected", "public",
            "return", "short", "static", "strictfp", "super",
            "switch", "synchronized", "this", "throw", "throws",
            "transient", "try", "void", "volatile", "while", "var"
    };

    private static final String KEYWORD_PATTERN = "\\b(" + String.join("|", KEYWORDS) + ")\\b";
    private static final String PAREN_PATTERN = "\\(|\\)";
    private static final String BRACE_PATTERN = "\\{|\\}";
    private static final String BRACKET_PATTERN = "\\[|\\]";
    private static final String SEMICOLON_PATTERN = "\\;";
    private static final String STRING_PATTERN = "\"([^\"\\\\]|\\\\.)*\"";
    private static final String COMMENT_PATTERN = "//[^\n]*" + "|" + "/\\*(.|\\R)*?\\*/";

    private static final Pattern PATTERN = Pattern.compile(
            "(?<KEYWORD>" + KEYWORD_PATTERN + ")"
                    + "|(?<PAREN>" + PAREN_PATTERN + ")"
                    + "|(?<BRACE>" + BRACE_PATTERN + ")"
                    + "|(?<BRACKET>" + BRACKET_PATTERN + ")"
                    + "|(?<SEMICOLON>" + SEMICOLON_PATTERN + ")"
                    + "|(?<STRING>" + STRING_PATTERN + ")"
                    + "|(?<COMMENT>" + COMMENT_PATTERN + ")"
    );

    /**
     * Constructor
     */
    public RichTextFXEditor() {
        this.codeArea = new CodeArea();
        this.codeArea.setParagraphGraphicFactory(LineNumberFactory.get(codeArea));
        this.scrollPane = new VirtualizedScrollPane<>(codeArea);

        // Re-highlight on text change
        codeArea.textProperty().addListener((obs, oldText, newText) -> {
            codeArea.setStyleSpans(0, computeHighlighting(newText));
        });

        Platform.runLater(() -> ThemeManager.register(this));
    }

    private static StyleSpans<Collection<String>> computeHighlighting(String text) {
        Matcher matcher = PATTERN.matcher(text);
        int lastKwEnd = 0;
        StyleSpansBuilder<Collection<String>> spansBuilder = new StyleSpansBuilder<>();
        while(matcher.find()) {
            String styleClass =
                    matcher.group("KEYWORD") != null ? "keyword" :
                    matcher.group("PAREN") != null ? "paren" :
                    matcher.group("BRACE") != null ? "brace" :
                    matcher.group("BRACKET") != null ? "bracket" :
                    matcher.group("SEMICOLON") != null ? "semicolon" :
                    matcher.group("STRING") != null ? "string" :
                    matcher.group("COMMENT") != null ? "comment" :
                    null; /* never happens */ assert styleClass != null;
            spansBuilder.add(Collections.emptyList(), matcher.start() - lastKwEnd);
            spansBuilder.add(Collections.singleton(styleClass), matcher.end() - matcher.start());
            lastKwEnd = matcher.end();
        }
        spansBuilder.add(Collections.emptyList(), text.length() - lastKwEnd);
        return spansBuilder.create();
    }

    @Override
    public Node getNode() {
        return scrollPane;
    }

    @Override
    public void refreshContent() {
        // RichTextFX usually handles its own repainting
    }

    @Override
    public void setText(String text) {
        codeArea.replaceText(text);
    }

    @Override
    public String getText() {
        return codeArea.getText();
    }

    @Override
    public void setSyntax(SyntaxType syntax) {
        this.currentSyntax = syntax;
        // Trigger re-highlighting
        codeArea.setStyleSpans(0, computeHighlighting(codeArea.getText()));
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
        codeArea.requestFollowCaret();
    }

    @Override
    public void applyTheme(IDETheme theme) {
        // In a real app, we would load a specific CSS file for the code area here.
        // For now, we rely on the global CSS from atlantafx and maybe add some basic styles.
        if (theme == IDETheme.DARK) {
            codeArea.setStyle("-fx-background-color: #2b2b2b; -fx-text-fill: #a9b7c6; -fx-font-family: 'Consolas'; -fx-font-size: 14pt;");
            codeArea.lookupAll(".line-number").forEach(node -> node.setStyle("-fx-text-fill: #606366;"));
        } else {
            codeArea.setStyle("-fx-background-color: #ffffff; -fx-text-fill: #000000; -fx-font-family: 'Consolas'; -fx-font-size: 14pt;");
            codeArea.lookupAll(".line-number").forEach(node -> node.setStyle("-fx-text-fill: #999999;"));
        }
    }
}
