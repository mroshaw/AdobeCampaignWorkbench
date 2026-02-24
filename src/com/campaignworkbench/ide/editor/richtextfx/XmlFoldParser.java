package com.campaignworkbench.ide.editor.richtextfx;

import org.fxmisc.richtext.CodeArea;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Implementation of IFoldParser that identifies fold regions in XML code
 */
public class XmlFoldParser extends FoldParser implements IFoldParser {

    private static final Pattern TAG_PATTERN =
            Pattern.compile("<(/?)([a-zA-Z0-9:_-]+)([^>]*)>");

    public XmlFoldParser(CodeArea codeArea) {
        super(codeArea);
    }

    public FoldRegions findFoldRegions(CodeArea codeArea) {

        foldRegions = new FoldRegions(codeArea);
        String text = codeArea.getText();

        Matcher matcher = TAG_PATTERN.matcher(text);
        Deque<Tag> stack = new ArrayDeque<>();

        while (matcher.find()) {

            String slash = matcher.group(1);
            String name = matcher.group(2);
            String rest = matcher.group(3);

            boolean selfClosing = rest != null && rest.trim().endsWith("/");

            if (slash.isEmpty() && !selfClosing) {
                // opening tag
                stack.push(new Tag(name, matcher.start()));
            } else if (!slash.isEmpty()) {
                // closing tag
                while (!stack.isEmpty()) {
                    Tag open = stack.pop();
                    int start = open.startOffset + 1;
                    int end = matcher.end();
                    if (open.name.equals(name) && start != end) {
                        foldRegions.add(start, end, foldedParagraphs);
                        break;
                    }
                }
            }
        }

        return foldRegions;
    }

    private static class Tag {
        final String name;
        final int startOffset;

        Tag(String name, int startOffset) {
            this.name = name;
            this.startOffset = startOffset;
        }
    }
}
