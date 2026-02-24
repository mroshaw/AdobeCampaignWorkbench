package com.campaignworkbench.ide.editor.richtextfx;

import org.fxmisc.richtext.CodeArea;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * An implementation of IFoldParser that identifies fold regions in Adobe Campaign template code
 */
public class CampaignFoldParser extends FoldParser implements IFoldParser {

    // Matches {, }, <% or %>
    private static final Pattern DELIM_PATTERN = Pattern.compile("(\\{)|(\\})|(<%)|(%>)");

    public CampaignFoldParser(CodeArea codeArea) {
        super(codeArea);
    }

    @Override
    public FoldRegions findFoldRegions(CodeArea codeArea) {

        foldRegions = new FoldRegions(codeArea);
        String text = codeArea.getText();

        Deque<Integer> braceStack = new ArrayDeque<>();
        Deque<Integer> tagStack = new ArrayDeque<>();

        Matcher matcher = DELIM_PATTERN.matcher(text);

        while (matcher.find()) {
            String match = matcher.group();

            switch (match) {
                case "{":
                    braceStack.push(matcher.start());
                    break;

                case "}":
                    if (!braceStack.isEmpty()) {
                        int start = braceStack.pop();
                        if (start != matcher.end()) {
                            foldRegions.add(start, matcher.end(), foldedParagraphs);
                        }
                    }
                    break;

                case "<%":
                    tagStack.push(matcher.start());
                    break;

                case "%>":
                    if (!tagStack.isEmpty()) {
                        int start = tagStack.pop();
                        if (start != matcher.end()) {
                            foldRegions.add(start, matcher.end(), foldedParagraphs);
                        }
                    }
                    break;
            }
        }

        return foldRegions;
    }
}