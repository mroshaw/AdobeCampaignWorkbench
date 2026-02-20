package com.campaignworkbench.ide.editor.richtextfx;

import com.campaignworkbench.ide.IDETheme;
import org.fxmisc.richtext.model.StyleSpans;
import org.fxmisc.richtext.model.StyleSpansBuilder;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

public class CampaignStyler implements ISyntaxStyler {

    // ECMAScript 3 keywords
    private static final String[] KEYWORDS = new String[] {
            // Reserved words - control structures
            "break", "case", "catch", "continue", "default", "do", "else",
            "finally", "for", "if", "return", "switch", "throw", "try", "while", "with",

            // Declarations
            "var", "function", "const", "let",

            // Operators / other reserved
            "this", "new", "delete", "typeof", "instanceof", "void", "in",

            // SpiderMonkey / JS 1.8.5 specific
            "yield",

            // Literals
            "true", "false", "null",

            // ADOBE CAMPAIGN KEYWORDS
            "include",
            // Global workflow logging
            "logInfo", "logError", "logWarning", "logVerbose",

            // Global utility functions
            "loadLibrary",
            "getUUID",
            "formatDate",
            "parseTimeStamp",
            "escapeXmlStr",
            "escapeUrl",
            "getUrl",

            // Cryptography / string helpers
            "cryptString", "decryptString", "encryptDES", "decryptDES",
            "digestStrMd5", "digestStrSha1", "digestStrSha224",
            "digestStrSha256", "digestStrSha384", "digestStrSha512",
            "hmacStr",

            // Standard SOAP API namespaces (objects)
            "NLWS",          // entry for SOAP methods on schemas
            "xtk",           // Campaign internal schema namespace objects (e.g., xtk.workflow, xtk.session)

            // Local workflow/global objects exposed by Campaign
            "instance",      // the current running workflow instance
            "task",          // the current workflow task
            "event",         // the event that triggered the task
            "events",        // list of invocation events
            "activity"
    };

    private static final String KEYWORD_PATTERN = "\\b(" + String.join("|", KEYWORDS) + ")\\b";
    private static final String HTML_PATTERN  = "<(/?\\w+)([^>]*)>";
    private static final String PAREN_PATTERN = "\\(|\\)";
    private static final String BRACE_PATTERN = "\\{|\\}|<%|%>";
    private static final String BRACKET_PATTERN = "\\[|\\]";
    private static final String SEMICOLON_PATTERN = "\\;";
    private static final String STRING_PATTERN =
            "\"([^\"\\\\]|\\\\.)*\"" +    // double-quoted strings
                    "|'([^'\\\\]|\\\\.)*'";       // single-quoted strings
    private static final String COMMENT_PATTERN = "//[^\n]*" + "|" + "/\\*(.|\\R)*?\\*/"   // for whole text processing (text blocks)
            + "|" + "/\\*[^\\v]*" + "|" + "^\\h*\\*([^\\v]*|/)";  // for visible paragraph processing (line by line)

    private static final String GROUP_KEYWORD = "KEYWORD";
    private static final String GROUP_HTML = "HTML";
    private static final String GROUP_PAREN = "PAREN";
    private static final String GROUP_BRACE = "BRACE";
    private static final String GROUP_BRACKET = "BRACKET";
    private static final String GROUP_SEMICOLON = "SEMICOLON";
    private static final String GROUP_STRING = "STRING";
    private static final String GROUP_COMMENT = "COMMENT";

    private static final Map<String, String> groupToStyleClass;

    static {
        groupToStyleClass = new HashMap<>();
        groupToStyleClass.put(GROUP_KEYWORD, "keyword");
        groupToStyleClass.put(GROUP_HTML, "keyword");
        groupToStyleClass.put(GROUP_PAREN, "paren");
        groupToStyleClass.put(GROUP_BRACE, "brace");
        groupToStyleClass.put(GROUP_BRACKET, "bracket");
        groupToStyleClass.put(GROUP_SEMICOLON, "semicolon");
        groupToStyleClass.put(GROUP_STRING, "string");
        groupToStyleClass.put(GROUP_COMMENT, "comment");

    }

    private static final Pattern PATTERN = Pattern.compile(
            "(?<" + GROUP_KEYWORD + ">" + KEYWORD_PATTERN + ")" +
                    "|(?<" + GROUP_PAREN + ">" + PAREN_PATTERN + ")" +
                    "|(?<" + GROUP_BRACE + ">" + BRACE_PATTERN + ")" +
                    "|(?<" + GROUP_BRACKET + ">" + BRACKET_PATTERN + ")" +
                    "|(?<" + GROUP_SEMICOLON + ">" + SEMICOLON_PATTERN + ")" +
                    "|(?<" + GROUP_STRING + ">" + STRING_PATTERN + ")" +
                    "|(?<" + GROUP_COMMENT + ">" + COMMENT_PATTERN + ")" +
                    "|(?<" + GROUP_HTML + ">" + HTML_PATTERN + ")"
    );

    private Matcher matcher;
    private StyleSpansBuilder<Collection<String>> spansBuilder;
    private int lastSpanEnd;

    public CampaignStyler() {

    }

    @Override
    public StyleSpans<Collection<String>> style(String text) {
        this.spansBuilder = new StyleSpansBuilder<>();
        this.lastSpanEnd = 0;
        this.matcher = PATTERN.matcher(text);
        while (matcher.find()) {
            String styleClass = evaluateNextStyle();
            evaluateMatch(matcher.start(), matcher.end(), styleClass);
        }
        completeStyleToEnd(text);
        return spansBuilder.create();
    }

    @Override
    public String getStyleSheet(IDETheme theme) {
        return theme.getCampaignStyleSheet();
    }

    private void completeStyleToEnd(String text) {
        spansBuilder.add(Collections.singleton("plain-text"), text.length() - lastSpanEnd);
    }

    private void evaluateMatch(int start, int end, String styleClass) {
        assert styleClass != null;
        // From the last style found to the new one we apply an empty list (no style)
        spansBuilder.add(Collections.singleton("plain-text"), start - lastSpanEnd);
        // Then we apply the one found by the matcher
        spansBuilder.add(Collections.singleton(styleClass), end - start);
        // Save the end
        lastSpanEnd = end;
    }

    private String evaluateNextStyle() {
        for (String groupName : groupToStyleClass.keySet()) {
            // If matcher found something that matches the group name, we return the associated style
            if (matcher.group(groupName) != null) {
                return groupToStyleClass.get(groupName);
            }
        }
        return null; /* never happens */
    }
}
