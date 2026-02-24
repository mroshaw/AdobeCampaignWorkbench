package com.campaignworkbench.ide.editor.richtextfx;


import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.model.TwoDimensional;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

/**
 * Class implementing a set of fold regions
 */
public class FoldRegions implements Iterable<FoldRegion> {

    // CodeArea is needed here to translate line numbers into paragraph indexes
    private final CodeArea codeArea;
    private final HashMap<Integer, FoldRegion> foldRegionMap;

    public FoldRegions(CodeArea codeArea) {
        this.codeArea = codeArea;
        foldRegionMap = new HashMap<>();
    }

    public void add(int startCharIndex, int endCharIndex, Set<Integer> foldedParagraphs) {

        // Get the lines as paragraph indexes
        int startParagraphIndex = getParagraphIndex(startCharIndex);
        int endParagraphIndex = getParagraphIndex(endCharIndex);

        // Check if it's already there or start and end are the same
        if (foldRegionMap.containsKey(startParagraphIndex) || startParagraphIndex == endParagraphIndex) {
            return;
        }
        boolean isFolded = foldedParagraphs.contains(startParagraphIndex);
        FoldRegion newRegion = new FoldRegion(startParagraphIndex, endParagraphIndex, isFolded);
        foldRegionMap.put(startParagraphIndex, newRegion);
    }

    public void remove(int paragraphIndex) {
        // If not in the map, do nothing
        if (!foldRegionMap.containsKey(paragraphIndex)) {
            return;
        }
        foldRegionMap.remove(paragraphIndex);
    }

    public void setFoldState(int paragraphIndex, boolean state) {
        // If not in the map, do nothing
        if (!foldRegionMap.containsKey(paragraphIndex)) {
            return;
        }
        foldRegionMap.get(paragraphIndex).setFoldedState(state);
    }

    private int getParagraphIndex(int lineNumber) {
        TwoDimensional.Position pos =
                codeArea.offsetToPosition(lineNumber, TwoDimensional.Bias.Forward);

        return pos.getMajor();
    }

    public Boolean isParagraphFoldable(int paragraphIndex) {
        return foldRegionMap.containsKey(paragraphIndex);
    }

    public Boolean isLineFoldable(int lineNumber) {
        return isParagraphFoldable(getParagraphIndex(lineNumber));
    }

    public boolean isParagraphHidden(int paragraphIndex) {
        for (FoldRegion region : foldRegionMap.values()) {
            // Only consider folded regions
            if (region.getFoldedState()) {
                // Check if paragraphIndex is inside the folded range
                if (region.isParagraphWithin(paragraphIndex)) {
                    return true;
                }
            }
        }
        return false;
    }

    public Boolean isParagraphFolded(int paragraphIndex) {
        if (!foldRegionMap.containsKey(paragraphIndex)) {
            return false;
        }

        return foldRegionMap.get(paragraphIndex).getFoldedState();
    }

    public int getFoldedParagraphEnd(int paragraphIndex) {
        if (!foldRegionMap.containsKey(paragraphIndex)) {
            return paragraphIndex;
        }

        return foldRegionMap.get(paragraphIndex).getEnd();
    }

    public Boolean isLineFolded(int lineNumber) {
        return isParagraphFolded(getParagraphIndex(lineNumber));
    }

    @Override
    public Iterator<FoldRegion> iterator() {
        return foldRegionMap.values().iterator();
    }

    public Iterable<FoldRegion> values() {
        return foldRegionMap.values();
    }

}
