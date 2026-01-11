import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.model.StyleSpans;
import org.fxmisc.richtext.model.StyleSpansBuilder;

import java.util.Collection;
import java.util.Collections;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SyntaxHighlighter {

    private static final String[] KEYWORDS = new String[]{
            "break", "case", "catch", "class", "const", "continue", "debugger", "default",
            "delete", "do", "else", "export", "extends", "finally", "for", "function",
            "if", "import", "in", "instanceof", "let", "new", "return", "super", "switch",
            "this", "throw", "try", "typeof", "var", "void", "while", "with", "yield"
    };

    private static final String KEYWORD_PATTERN = "\\b(" + String.join("|", KEYWORDS) + ")\\b";
    private static final String PAREN_PATTERN = "[()]";
    private static final String BRACE_PATTERN = "[{}]";
    private static final String BRACKET_PATTERN = "[\\[\\]]";
    private static final String SEMICOLON_PATTERN = ";";
    private static final String STRING_PATTERN = "\"([^\"\\\\]|\\\\.)*\"|'([^'\\\\]|\\\\.)*'";
    private static final String COMMENT_PATTERN = "//[^\n]*|/\\*(.|\\R)*?\\*/";

    private static final Pattern PATTERN = Pattern.compile(
            "(?<KEYWORD>" + KEYWORD_PATTERN + ")"
                    + "|(?<PAREN>" + PAREN_PATTERN + ")"
                    + "|(?<BRACE>" + BRACE_PATTERN + ")"
                    + "|(?<BRACKET>" + BRACKET_PATTERN + ")"
                    + "|(?<SEMICOLON>" + SEMICOLON_PATTERN + ")"
                    + "|(?<STRING>" + STRING_PATTERN + ")"
                    + "|(?<COMMENT>" + COMMENT_PATTERN + ")"
    );

    /** Apply highlighting to a CodeArea (immediate + dynamic). */
    public static void applyHighlighting(CodeArea codeArea) {
        // Highlight initial content
        codeArea.setStyleSpans(0, computeHighlighting(codeArea.getText()));

        // Highlight on edits
        codeArea.textProperty().addListener((obs, oldText, newText) -> {
            codeArea.setStyleSpans(0, computeHighlighting(newText));
        });
    }

    /** Compute the StyleSpans for the given text */
    public static StyleSpans<Collection<String>> computeHighlighting(String text) {
        Matcher matcher = PATTERN.matcher(text);
        int lastEnd = 0;
        StyleSpansBuilder<Collection<String>> spansBuilder = new StyleSpansBuilder<>();

        while (matcher.find()) {
            String styleClass;
            if (matcher.group("KEYWORD") != null) styleClass = "keyword";
            else if (matcher.group("PAREN") != null) styleClass = "paren";
            else if (matcher.group("BRACE") != null) styleClass = "brace";
            else if (matcher.group("BRACKET") != null) styleClass = "bracket";
            else if (matcher.group("SEMICOLON") != null) styleClass = "semicolon";
            else if (matcher.group("STRING") != null) styleClass = "string";
            else if (matcher.group("COMMENT") != null) styleClass = "comment";
            else styleClass = null;

            spansBuilder.add(Collections.emptyList(), matcher.start() - lastEnd);
            if (styleClass != null) {
                spansBuilder.add(Collections.singleton(styleClass), matcher.end() - matcher.start());
            } else {
                spansBuilder.add(Collections.emptyList(), matcher.end() - matcher.start());
            }
            lastEnd = matcher.end();
        }

        spansBuilder.add(Collections.emptyList(), text.length() - lastEnd);
        return spansBuilder.create();
    }
}
