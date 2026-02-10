package com.campaignworkbench.ide.editor;

import com.campaignworkbench.ide.IDETheme;
import com.campaignworkbench.ide.IThemeable;
import com.campaignworkbench.ide.ThemeManager;
import javafx.application.Platform;
import javafx.scene.Node;
import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.LineNumberFactory;
import org.fxmisc.richtext.model.StyleSpans;
import org.fxmisc.richtext.model.StyleSpansBuilder;
import org.fxmisc.flowless.VirtualizedScrollPane;
import javafx.scene.shape.Polygon;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.layout.Priority;
import javafx.scene.Cursor;
import javafx.geometry.Pos;
import java.util.function.IntFunction;

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
    private SyntaxType currentSyntax = SyntaxType.PLAIN;

    // Folding support
    private final java.util.Set<Integer> foldedParagraphs = new java.util.HashSet<>();
    private final java.util.Map<Integer, String> foldedContents = new java.util.HashMap<>();

    private static final String[] JS_KEYWORDS = new String[] {
            "break", "case", "catch", "class", "const", "continue", "debugger", "default", "delete", "do",
            "else", "export", "extends", "finally", "for", "function", "if", "import", "in", "instanceof",
            "new", "return", "super", "switch", "this", "throw", "try", "typeof", "var", "void", "while", "with",
            "yield", "let", "static", "enum", "await", "async", "include"
    };

    private static final String JS_KEYWORD_PATTERN = "\\b(" + String.join("|", JS_KEYWORDS) + ")\\b";
    private static final String JS_PAREN_PATTERN = "\\(|\\)";
    private static final String JS_BRACE_PATTERN = "\\{|\\}";
    private static final String JS_BRACKET_PATTERN = "\\[|\\]";
    private static final String JS_SEMICOLON_PATTERN = "\\;";
    private static final String JS_STRING_PATTERN = "\"([^\"\\\\]|\\\\.)*\"|'([^'\\\\]|\\\\.)*'";
    private static final String JS_COMMENT_PATTERN = "//[^\n]*" + "|" + "/\\*(.|\\R)*?\\*/";

    private static final Pattern JS_PATTERN = Pattern.compile(
            "(?<KEYWORD>" + JS_KEYWORD_PATTERN + ")"
                    + "|(?<PAREN>" + JS_PAREN_PATTERN + ")"
                    + "|(?<BRACE>" + JS_BRACE_PATTERN + ")"
                    + "|(?<BRACKET>" + JS_BRACKET_PATTERN + ")"
                    + "|(?<SEMICOLON>" + JS_SEMICOLON_PATTERN + ")"
                    + "|(?<STRING>" + JS_STRING_PATTERN + ")"
                    + "|(?<COMMENT>" + JS_COMMENT_PATTERN + ")"
    );

    // XML highlighting
    private static final String XML_TAG_PATTERN = "<(/?[-_a-zA-Z0-9]+)"; // simpler, no nested groups
    private static final String XML_ATTRIBUTE_PATTERN = "\\b([-_a-zA-Z0-9]+)(?=\\s*=)";
    private static final String XML_STRING_PATTERN = "\"[^\"]*\"|'[^']*'"; // no backtracking over \\
    private static final String XML_COMMENT_PATTERN = "<!--[^-]*(-[^-]+)*-->";


    private static final Pattern XML_PATTERN = Pattern.compile(
            "(?<TAG>" + XML_TAG_PATTERN + ")"
                    + "|(?<ATTRIBUTE>" + XML_ATTRIBUTE_PATTERN + ")"
                    + "|(?<STRING>" + XML_STRING_PATTERN + ")"
                    + "|(?<COMMENT>" + XML_COMMENT_PATTERN + ")"
    );

    // JSP / Template highlighting (Integrated JS and XML)
    private static final String JSP_SCRIPTLET_PATTERN = "<%([\\s\\S]*?)%>";

    private static final Pattern JSP_PATTERN = Pattern.compile(
            "(?<SCRIPTLET>" + JSP_SCRIPTLET_PATTERN + ")"
                    + "|(?<TAG>" + XML_TAG_PATTERN + ")"
                    + "|(?<ATTRIBUTE>" + XML_ATTRIBUTE_PATTERN + ")"
                    + "|(?<STRING>" + XML_STRING_PATTERN + ")"
                    + "|(?<COMMENT>" + XML_COMMENT_PATTERN + ")"
    );

    /**
     * Constructor
     */
    public RichTextFXEditor() {
        this.codeArea = new CodeArea();
        this.codeArea.setCursor(Cursor.TEXT);

        this.scrollPane = new VirtualizedScrollPane<>(codeArea);
        VBox.setVgrow(codeArea, Priority.ALWAYS);

        IntFunction<Node> baseNumberFactory = LineNumberFactory.get(codeArea, digits -> "%" + Math.max(3, digits) + "d ");
        IntFunction<Node> numberFactory = line -> {
            if (codeArea.getText().trim().isEmpty()) {
                return new javafx.scene.layout.Region();
            }
            Node node = baseNumberFactory.apply(line);
            node.getStyleClass().add("lineno");
            return node;
        };
        IntFunction<Node> foldingFactory = new FoldingFactory();
        IntFunction<Node> graphicFactory = line -> {
            HBox hbox = new HBox(numberFactory.apply(line), foldingFactory.apply(line));
            hbox.getStyleClass().add("gutter");
            hbox.setAlignment(Pos.CENTER_RIGHT);
            return hbox;
        };
        this.codeArea.setParagraphGraphicFactory(graphicFactory);
        
        // Re-highlight on text change
        codeArea.textProperty().addListener((obs, oldText, newText) -> {
            codeArea.setStyleSpans(0, computeHighlighting(newText));
        });

        Platform.runLater(() -> ThemeManager.register(this));
    }

    private class FoldingFactory implements IntFunction<Node> {
        @Override
        public Node apply(int lineNumber) {
            Polygon triangle = new Polygon(0.0, 0.0, 8.0, 4.0, 0.0, 8.0);
            triangle.getStyleClass().add("folding-triangle");
            HBox.setMargin(triangle, new javafx.geometry.Insets(0, 0, 0, -2));
            
            // Logic to check if this line is foldable
            if (isFoldable(lineNumber)) {
                triangle.setVisible(true);
                triangle.setRotate(foldedParagraphs.contains(lineNumber) ? 0 : 90);
                triangle.setCursor(Cursor.HAND);
                triangle.setOnMouseClicked(e -> toggleFold(lineNumber));
            } else {
                triangle.setVisible(false);
            }
            
            return triangle;
        }
    }

    private boolean isFoldable(int lineNumber) {
        String text = codeArea.getParagraph(lineNumber).getText();
        if (currentSyntax == SyntaxType.BLOCK || currentSyntax == SyntaxType.SOURCE_PREVIEW || currentSyntax == SyntaxType.TEMPLATE) {
            return text.contains("{");
        } else if (currentSyntax == SyntaxType.XML || currentSyntax == SyntaxType.HTML_PREVIEW) {
            return text.contains("<") && !text.contains("</") && !text.contains("/>");
        }
        return false;
    }

    private void toggleFold(int lineNumber) {
        if (foldedParagraphs.contains(lineNumber)) {
            unfold(lineNumber);
        } else {
            fold(lineNumber);
        }
    }

    private void fold(int lineNumber) {
        // Find matching closing brace/tag
        int endLine = findClosingElement(lineNumber);
        if (endLine > lineNumber) {
            // Simple folding: hide text between lineNumber and endLine
            // RichTextFX doesn't easily support hiding paragraphs in CodeArea without specialized models.
            // As a workaround, we'll replace the text with a placeholder.
            // Note: This is a simplified approach.
            
            int startOffset = codeArea.getAbsolutePosition(lineNumber, codeArea.getParagraph(lineNumber).length());
            int endOffset = codeArea.getAbsolutePosition(endLine, codeArea.getParagraph(endLine).length());
            
            String originalText = codeArea.getText(startOffset, endOffset);
            foldedContents.put(lineNumber, originalText);
            foldedParagraphs.add(lineNumber);
            
            String placeholder = (currentSyntax == SyntaxType.XML || currentSyntax == SyntaxType.HTML_PREVIEW) ? " ... " : " { ... }";
            codeArea.replaceText(startOffset, endOffset, placeholder);
        }
    }

    private void unfold(int lineNumber) {
        String originalText = foldedContents.remove(lineNumber);
        if (originalText != null) {
            String placeholder = (currentSyntax == SyntaxType.XML || currentSyntax == SyntaxType.HTML_PREVIEW) ? " ... " : " { ... }";
            int startOffset = codeArea.getAbsolutePosition(lineNumber, codeArea.getParagraph(lineNumber).getText().indexOf(placeholder));
            int endOffset = startOffset + placeholder.length();
            
            codeArea.replaceText(startOffset, endOffset, originalText);
            foldedParagraphs.remove(lineNumber);
        }
    }

    private int findClosingElement(int startLine) {
        String startText = codeArea.getParagraph(startLine).getText();
        if (startText.contains("{")) {
            int openBraces = 0;
            for (int i = startLine; i < codeArea.getParagraphs().size(); i++) {
                String lineText = codeArea.getParagraph(i).getText();
                for (char c : lineText.toCharArray()) {
                    if (c == '{') openBraces++;
                    else if (c == '}') {
                        openBraces--;
                        if (openBraces == 0) return i;
                    }
                }
            }
        } else if (currentSyntax == SyntaxType.XML || currentSyntax == SyntaxType.HTML_PREVIEW) {
            // Find the tag name at the start line
            Pattern tagPattern = Pattern.compile("<(?<TAGNAME>[-_a-zA-Z0-9]+)");
            Matcher matcher = tagPattern.matcher(startText);
            if (matcher.find()) {
                String tagName = matcher.group("TAGNAME");
                String openingTag = "<" + tagName;
                String closingTag = "</" + tagName + ">";
                
                int openTags = 0;
                for (int i = startLine; i < codeArea.getParagraphs().size(); i++) {
                    String lineText = codeArea.getParagraph(i).getText();
                    
                    // Simple count of opening and closing tags for this specific tag name
                    // This is still a bit simplified as it doesn't handle tags spanning multiple lines perfectly,
                    // but it's better than nothing and should work for most well-formatted XML.
                    
                    int index = 0;
                    while ((index = lineText.indexOf(openingTag, index)) != -1) {
                        // Check if it's not a closing tag and not self-closing
                        if (index == 0 || lineText.charAt(index - 1) != '/') {
                            // verify it's a full tag name match
                            int endOfName = index + openingTag.length();
                            if (endOfName >= lineText.length() || !Character.isLetterOrDigit(lineText.charAt(endOfName))) {
                                // Check if it's NOT self-closing on the same line
                                int tagEnd = lineText.indexOf(">", index);
                                if (tagEnd != -1 && lineText.charAt(tagEnd - 1) != '/') {
                                    openTags++;
                                }
                            }
                        }
                        index += openingTag.length();
                    }
                    
                    index = 0;
                    while ((index = lineText.indexOf(closingTag, index)) != -1) {
                        openTags--;
                        if (openTags == 0) return i;
                        index += closingTag.length();
                    }
                }
            }
        }
        return -1;
    }

    private StyleSpans<Collection<String>> computeHighlighting(String text) {
        Pattern pattern = switch (currentSyntax) {
            case TEMPLATE -> JSP_PATTERN;
            case BLOCK, SOURCE_PREVIEW -> JS_PATTERN;
            case XML -> XML_PATTERN;
            case HTML_PREVIEW -> XML_PATTERN; // Use XML pattern for HTML for now
            default -> null;
        };

        if (pattern == null) {
            return new StyleSpansBuilder<Collection<String>>().add(Collections.emptyList(), text.length()).create();
        }

        Matcher matcher = pattern.matcher(text);
        int lastKwEnd = 0;
        StyleSpansBuilder<Collection<String>> spansBuilder = new StyleSpansBuilder<>();
        while(matcher.find()) {
            String styleClass = getStyleClass(matcher);

            spansBuilder.add(Collections.emptyList(), matcher.start() - lastKwEnd);

            if (currentSyntax == SyntaxType.TEMPLATE && "scriptlet".equals(styleClass)) {
                // Nested highlighting for scriptlets
                String scriptletText = matcher.group("SCRIPTLET");
                int innerStart = matcher.start();
                
                // Add scriptlet background but also process inner JS
                // Since RichTextFX only supports one set of style classes per range easily with this builder,
                // we'll combine "scriptlet" with other classes.
                
                processScriptlet(spansBuilder, scriptletText, innerStart);
            } else {
                spansBuilder.add(Collections.singleton(styleClass), matcher.end() - matcher.start());
            }
            lastKwEnd = matcher.end();
        }
        spansBuilder.add(Collections.emptyList(), text.length() - lastKwEnd);
        return spansBuilder.create();
    }

    private void processScriptlet(StyleSpansBuilder<Collection<String>> spansBuilder, String scriptletText, int offset) {
        // Strip <% and %>
        String content = scriptletText.substring(2, scriptletText.length() - 2);
        
        // Add style for <%
        spansBuilder.add(java.util.List.of("scriptlet", "tag"), 2);
        
        Matcher jsMatcher = JS_PATTERN.matcher(content);
        int lastInnerEnd = 0;
        while (jsMatcher.find()) {
            // Text between matches
            if (jsMatcher.start() > lastInnerEnd) {
                spansBuilder.add(Collections.singleton("scriptlet"), jsMatcher.start() - lastInnerEnd);
            }
            
            // The match itself
            String jsStyleClass = getJSStyleClass(jsMatcher);
            spansBuilder.add(java.util.List.of("scriptlet", jsStyleClass), jsMatcher.end() - jsMatcher.start());
            lastInnerEnd = jsMatcher.end();
        }
        
        if (lastInnerEnd < content.length()) {
            spansBuilder.add(Collections.singleton("scriptlet"), content.length() - lastInnerEnd);
        }
        
        // Add style for %>
        spansBuilder.add(java.util.List.of("scriptlet", "tag"), 2);
    }

    private String getJSStyleClass(Matcher matcher) {
        if (matcher.group("KEYWORD") != null) return "keyword";
        if (matcher.group("PAREN") != null) return "paren";
        if (matcher.group("BRACE") != null) return "brace";
        if (matcher.group("BRACKET") != null) return "bracket";
        if (matcher.group("SEMICOLON") != null) return "semicolon";
        if (matcher.group("STRING") != null) return "string";
        if (matcher.group("COMMENT") != null) return "comment";
        return "plain";
    }

    private String getStyleClass(Matcher matcher) {
        if (currentSyntax == SyntaxType.BLOCK || currentSyntax == SyntaxType.SOURCE_PREVIEW) {
            if (matcher.group("KEYWORD") != null) return "keyword";
            if (matcher.group("PAREN") != null) return "paren";
            if (matcher.group("BRACE") != null) return "brace";
            if (matcher.group("BRACKET") != null) return "bracket";
            if (matcher.group("SEMICOLON") != null) return "semicolon";
            if (matcher.group("STRING") != null) return "string";
            if (matcher.group("COMMENT") != null) return "comment";
        } else if (currentSyntax == SyntaxType.XML || currentSyntax == SyntaxType.HTML_PREVIEW || currentSyntax == SyntaxType.TEMPLATE) {
            if (matcher.group("TAG") != null) return "tag";
            if (matcher.group("ATTRIBUTE") != null) return "attribute";
            if (matcher.group("STRING") != null) return "string";
            if (matcher.group("COMMENT") != null) return "comment";
            if (currentSyntax == SyntaxType.TEMPLATE && matcher.group("SCRIPTLET") != null) return "scriptlet";
        }
        return "plain";
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
        codeArea.selectLine();
        codeArea.requestFollowCaret();
    }

    @Override
    public void applyTheme(IDETheme theme) {
        // Use AtlantaFX CSS variables for theme integration
        // -fx-text-fill doesn't work for CodeArea text, we need to style the .text class
        codeArea.setStyle("-fx-font-family: 'Consolas'; -fx-font-size: 11pt;");

        if (theme == IDETheme.DARK) {
            // Define colors for dark theme - these can also use AtlantaFX variables if suitable ones exist
            // but for syntax highlighting we usually want specific colors.
            // Using AtlantaFX accent colors where possible.
            String keywordColor = "-color-accent-fg";
            String stringColor = "-color-success-fg";
            String commentColor = "-color-fg-subtle";
            String tagColor = "-color-accent-fg";
            String attributeColor = "-color-warning-fg";
            String scriptletColor = "-color-fg-default";
            String symbolColor = "-color-fg-muted";

            applyHighlightingStyles(keywordColor, stringColor, commentColor, tagColor, attributeColor, scriptletColor, symbolColor);
        } else {
            // Define colors for light theme
            String keywordColor = "-color-accent-emphasis";
            String stringColor = "-color-success-emphasis";
            String commentColor = "-color-fg-subtle";
            String tagColor = "-color-accent-emphasis";
            String attributeColor = "-color-warning-emphasis";
            String scriptletColor = "-color-fg-default";
            String symbolColor = "-color-fg-muted";

            applyHighlightingStyles(keywordColor, stringColor, commentColor, tagColor, attributeColor, scriptletColor, symbolColor);
        }
    }

    @Override
    public void openFindDialog(String fileName) {

    }

    private void applyHighlightingStyles(String keyword, String string, String comment, String tag, String attribute, String scriptlet, String symbol) {
        String css = ".code-area { " +
        // "  -fx-background-color: linear-gradient(to right, -color-bg-subtle 0%, -color-bg-subtle 60px, -color-bg-default 60px, -color-bg-default 100%); " +
                "-fx-background-color: -color-bg-subtle, linear-gradient(to right, -color-bg-default, -color-bg-default); " +
                "-fx-background-insets: 0, 0 0 0 40;" +
                "-fx-tab-size: 4;" +
                "}\n" +
                ".virtualized-scroll-pane { -fx-background-color: -color-bg-default; }\n" +
                ".code-area .text { -fx-fill: -color-fg-default; }\n" +
                ".code-area .paragraph-text {-fx-tab-size: 2}; \n" +
                ".paragraph-box { -fx-background-color: transparent; }\n" +
                ".gutter { -fx-background-color: transparent; }\n" +
                //".gutter { -fx-background-color: transparent; -fx-padding: 0 5 0 0; -fx-min-width: 60px; -fx-max-width: 60px; }\n" +
                ".keyword { -fx-fill: " + keyword + " !important; -fx-font-weight: bold; }\n" +
                ".string { -fx-fill: " + string + " !important; }\n" +
                ".comment { -fx-fill: " + comment + " !important; }\n" +
                ".tag { -fx-fill: " + tag + " !important; -fx-font-weight: bold; }\n" +
                ".attribute { -fx-fill: " + attribute + " !important; }\n" +
                ".scriptlet { -fx-background-color: -color-accent-subtle !important; }\n" +
                ".paren { -fx-fill: " + symbol + " !important; }\n" +
                ".brace { -fx-fill: " + symbol + " !important; }\n" +
                ".bracket { -fx-fill: " + symbol + " !important; }\n" +
                ".semicolon { -fx-fill: " + symbol + " !important; }\n" +
                ".caret { -fx-stroke: -color-fg-default !important; }\n" +
                ".folding-triangle { -fx-fill: -color-fg-muted !important; }\n" +
                ".folding-triangle:hover { -fx-fill: -color-accent-fg !important; }\n" +
                ".lineno { -fx-text-fill: -color-fg-muted !important; -fx-background-color: transparent !important; -fx-font-family: 'Consolas'; -fx-font-size: 11pt; }";

        codeArea.getStylesheets().clear();
        scrollPane.getStylesheets().clear();
        try {
            String encodedCss = java.net.URLEncoder.encode(css, java.nio.charset.StandardCharsets.UTF_8).replace("+", "%20");
            String dataUri = "data:text/css," + encodedCss;
            codeArea.getStylesheets().add(dataUri);
            scrollPane.getStylesheets().add(dataUri);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
