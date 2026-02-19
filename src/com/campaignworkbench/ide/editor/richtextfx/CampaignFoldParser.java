package com.campaignworkbench.ide.editor.richtextfx;

import org.fxmisc.richtext.CodeArea;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Produces fold regions for well-formed XML:
 *  - Skips comments, CDATA, processing instructions, and DOCTYPE.
 *  - Distinguishes closing vs self-closing tags using explicit capture groups.
 *  - Returns regions as (startLine, endLine) inclusive of the closing tag line.
 *
 * NOTE: Keep using end+1 (start of next line) as the exclusive end WHEN calling foldText().
 */
public class CampaignFoldParser implements IFoldParser {

    // <(/?)(name)(attributes?) (/)?>
    //  group(1): "/" if closing
    //  group(2): tag name (letters, digits, ":", "_", "-")
    //  group(3): attributes (may be empty)
    //  group(4): "/" if self-closing (right before '>')
    private static final Pattern TAG_PATTERN = Pattern.compile(
            "<(/?)([A-Za-z0-9:_-]+)([^>]*)?(\\/)?>"
    );

    // Case-insensitive for <!DOCTYPE ...>
    private static final Pattern DOCTYPE_WORD = Pattern.compile("!DOCTYPE", Pattern.CASE_INSENSITIVE);
    private FoldRegions foldRegions;

    @Override
    public FoldRegions findFoldRegions(CodeArea codeArea, Set<Integer> foldedParagraphs) {
        foldRegions = new FoldRegions(codeArea);
        String text = codeArea.getText();

        Deque<TagInfo> stack = new ArrayDeque<>();

        // Keep trailing empty line as a paragraph so indices map 1:1 to CodeArea
        String[] lines = text.split("\n", -1);

        boolean inComment = false; // <!-- ... -->
        boolean inCdata   = false; // <![CDATA[ ... ]]>

        for (int lineIndex = 0; lineIndex < lines.length; lineIndex++) {
            String line = lines[lineIndex];
            int i = 0;
            final int n = line.length();

            while (i < n) {
                if (inComment) {
                    int end = line.indexOf("-->", i);
                    if (end >= 0) { inComment = false; i = end + 3; } else break;
                    continue;
                }
                if (inCdata) {
                    int end = line.indexOf("]]>", i);
                    if (end >= 0) { inCdata = false; i = end + 3; } else break;
                    continue;
                }

                int lt = line.indexOf('<', i);
                if (lt < 0) break; // nothing else to parse on this line
                i = lt;

                // Check non-element constructs first
                if (startsWith(line, i, "<!--")) {
                    inComment = true;
                    i += 4; // consume "<!--"
                    continue;
                }
                if (startsWith(line, i, "<![CDATA[")) {
                    inCdata = true;
                    i += 9; // consume "<![CDATA["
                    continue;
                }
                if (startsWith(line, i, "<?")) {
                    // processing instruction: skip to next '>'
                    int close = line.indexOf('>', i + 2);
                    i = (close >= 0) ? close + 1 : n;
                    continue;
                }
                if (startsWithIgnoreCase(line, i, "<!DOCTYPE")) {
                    // doctype: skip to next '>'
                    int close = line.indexOf('>', i + 2);
                    i = (close >= 0) ? close + 1 : n;
                    continue;
                }

                // Try to match an element tag from this position
                Matcher m = TAG_PATTERN.matcher(line);
                m.region(i, n);
                if (!m.lookingAt()) {
                    // not a tag (e.g., "<" in text), skip one char to avoid infinite loop
                    i++;
                    continue;
                }

                String slash  = m.group(1);   // "/" if closing
                String name   = m.group(2);
                String self   = m.group(4);   // "/" if self-closing

                boolean isClosing     = "/".equals(slash);
                boolean isSelfClosing = "/".equals(self);

                if (!isClosing && !isSelfClosing) {
                    // Opening tag
                    stack.push(new TagInfo(name, lineIndex));
                } else if (isClosing) {
                    // Closing tag — match nearest same-name open
                    TagInfo open = findAndRemoveNearest(stack, name);
                    if (open != null && open.lineIndex < lineIndex) {
                        foldRegions.add(open.lineIndex, lineIndex, foldedParagraphs);
                    }
                }
                i = m.end(); // advance past the matched tag
            }
        }

        // Unmatched opens are ignored for folding
        return foldRegions;
    }

    private static boolean startsWith(String s, int from, String prefix) {
        int end = from + prefix.length();
        return s.length() >= end && s.regionMatches(from, prefix, 0, prefix.length());
    }

    private static boolean startsWithIgnoreCase(String s, int from, String prefix) {
        int end = from + prefix.length();
        return s.length() >= end && s.regionMatches(true, from, prefix, 0, prefix.length());
    }

    private TagInfo findAndRemoveNearest(Deque<TagInfo> stack, String tagName) {
        for (Iterator<TagInfo> it = stack.iterator(); it.hasNext();) {
            TagInfo info = it.next();
            if (info.tagName.equals(tagName)) {
                it.remove();
                return info;
            }
        }
        return null; // unmatched close
    }

    private static final class TagInfo {
        final String tagName;
        final int lineIndex;
        TagInfo(String tagName, int lineIndex) {
            this.tagName = tagName;
            this.lineIndex = lineIndex;
        }
    }
}