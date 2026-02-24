package com.campaignworkbench.test;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.fxmisc.flowless.VirtualizedScrollPane;
import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.LineNumberFactory;

import java.util.*;
import java.util.function.IntFunction;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FoldTest extends Application {

    public record FoldRegion(int startParagraph, int endParagraph) {}
    private record TagInfo(String tagName, int lineIndex) {}

    private static final Pattern TAG_PATTERN = Pattern.compile(
            "<(/?)([A-Za-z0-9:_-]+)([^>]*)?(\\/)?>"
    );

    private VBox parent;
    private CodeArea codeArea;
    private IntFunction<Node> numberFactory;
    private List<FoldRegion> foldRegions = new ArrayList<>();
    private final java.util.Map<Integer, Integer> foldStartToEnd = new java.util.HashMap<>();
    private final java.util.Set<Integer> collapsedStarts = new java.util.HashSet<>();
    private IntFunction<Node> gutterFactory = this::gutterFor;

    @Override
    public void start(Stage stage) {

        codeArea = new CodeArea();

        numberFactory = LineNumberFactory.get(codeArea);
        codeArea.setParagraphGraphicFactory(numberFactory);


        final String sample =
                "<root>\n" +
                        "  <a>\n" +
                        "    <b>one</b>\n" +
                        "    <b>two</b>\n" +
                        "  </a>\n" +
                        "  <c>last</c>\n" +
                        "</root>\n";

        codeArea.textProperty().addListener((obs, oldText, newText) -> {
            updateFolding(codeArea.getText());
        });

        BorderPane codeAreaContainer = new BorderPane(new VirtualizedScrollPane<>(codeArea));
        parent = new VBox(codeAreaContainer);
        parent.getStyleClass().add("code-editor");
        VBox.setVgrow(codeAreaContainer, Priority.ALWAYS);
        stage.setScene(new Scene(parent, 640, 420));
        stage.setTitle("RichTextFX folding — offset pairing");
        stage.show();
        codeArea.replaceText(sample);
    }

    private void updateFolding(String text) {
        foldRegions = getFoldRegions(text);

        // Rebuild start->end index for quick lookup
        foldStartToEnd.clear();
        for (FoldRegion r : foldRegions) {
            // defensive checks: ignore degenerate regions
            if (r != null && r.startParagraph() >= 0 && r.endParagraph() > r.startParagraph()) {
                foldStartToEnd.put(r.startParagraph(), r.endParagraph());
            }
        }

        // Remove any collapsed starts that no longer exist after edits
        collapsedStarts.removeIf(s -> !foldStartToEnd.containsKey(s));

        // Optional: clear/add paragraph CSS marker (if you want to style foldable lines)
        for (int i = 0; i < codeArea.getParagraphs().size(); i++) {
            codeArea.setParagraphStyle(i, Collections.emptyList());
        }
        for (FoldRegion region : foldRegions) {
            codeArea.setParagraphStyle(region.startParagraph(), Collections.singletonList("foldable"));
        }

        // Refresh gutter for visible paragraphs by reapplying the factory
        codeArea.setParagraphGraphicFactory(line -> gutterFactory.apply(line));
    }

    private Node gutterFor(int paragraphIndex) {
        // Base line-number node
        Node ln = numberFactory.apply(paragraphIndex);

        // A small, fixed-width container for the toggle so line numbers stay aligned
        Label toggle = new Label(); // we'll set text and handlers if foldable
        // toggle.getStyleClass().add("fold-toggle");
        toggle.setMinWidth(12);
        toggle.setPrefWidth(12);
        toggle.setAlignment(Pos.CENTER);
        toggle.setPadding(Insets.EMPTY);
        toggle.setOnMouseClicked(null); // default: inactive

        // Is this paragraph a fold start?
        Integer end = foldStartToEnd.get(paragraphIndex);
        if (end != null && end > paragraphIndex) {
            boolean isCollapsed = collapsedStarts.contains(paragraphIndex);
            toggle.setText(isCollapsed ? "⊞" : "⊟"); // plus when collapsed, minus when expanded
            toggle.getStyleClass().add(isCollapsed ? "fold-toggle-collapsed" : "fold-toggle-expanded");

            toggle.addEventHandler(MouseEvent.MOUSE_CLICKED, e -> {
                e.consume(); // don't move caret
                toggleFold(paragraphIndex);
            });
            toggle.setCursor(Cursor.HAND);
        } else {
            // Spacer keeps alignment even when not foldable
            toggle.setText(""); // nothing
            toggle.setMouseTransparent(true);
            toggle.getStyleClass().add("fold-toggle-empty");
        }

        HBox box = new HBox(6, toggle, ln);
        box.setAlignment(Pos.CENTER_LEFT);
        box.getStyleClass().add("gutter");
        return box;
    }

    private void toggleFold(int startPar) {

        if (collapsedStarts.contains(startPar)) {
            // Unfold the region that starts at startOffset (must pair with foldText)
            codeArea.unfoldParagraphs(startPar);
            collapsedStarts.remove(startPar);
        } else {
            // Fold offsets in [startOffset, endOffset)
            codeArea.foldParagraphs(startPar, startPar + 1);
            collapsedStarts.add(startPar);
        }
    }

    public List<FoldRegion> getFoldRegions(String text) {
        List<FoldRegion> regions = new ArrayList<>();
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
                        regions.add(new FoldRegion(open.lineIndex, lineIndex));
                    }
                }
                i = m.end(); // advance past the matched tag
            }
        }

        // Unmatched opens are ignored for folding
        return regions;
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

    static void main(String[] args) { launch(args); }
}
