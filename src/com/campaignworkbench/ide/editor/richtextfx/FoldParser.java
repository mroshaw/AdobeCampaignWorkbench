package com.campaignworkbench.ide.editor.richtextfx;

import org.fxmisc.richtext.CodeArea;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Base class for implementing code folding solutions for various languages
 */
public abstract class FoldParser {

    protected FoldRegions foldRegions;
    protected final Set<Integer> foldedParagraphs;
    CodeArea codeArea;

    public FoldParser(CodeArea codeArea) {
        this.codeArea = codeArea;
        foldRegions = new FoldRegions(codeArea);
        foldedParagraphs = new HashSet<>();
    }

    public void unfoldAll() {
        List<Integer> list = new ArrayList<>(foldedParagraphs);
        for (Integer paragraphIndex : list) {
            unfoldParagraph(paragraphIndex);
        }
    }

    public void foldAll() {
        for (FoldRegion foldRegion : foldRegions) {
            if(!foldRegion.getFoldedState()) {
                foldParagraph(foldRegion.getStart());
            }
        }
    }

    public void foldParagraph(int startParagraphIndex) {
        addFoldedParagraph(startParagraphIndex);
        int endParagraphIndex = foldRegions.getFoldedParagraphEnd(startParagraphIndex);
        codeArea.foldParagraphs(startParagraphIndex, endParagraphIndex );
    }

    public void unfoldParagraph(int startParagraphIndex) {
        removeFoldedParagraph(startParagraphIndex);
        codeArea.unfoldParagraphs(startParagraphIndex);
    }

    public void addFoldedParagraph(int startParagraphIndex) {
        foldedParagraphs.add(startParagraphIndex);
    }

    public void removeFoldedParagraph(int startParagraphIndex) {
        foldedParagraphs.remove(startParagraphIndex);
    }

    public boolean isParagraphFolded(int paragraphIndex) {
        return foldedParagraphs.contains(paragraphIndex);
    }
}
