package com.campaignworkbench.ide.editor.richtextfx;

import org.fxmisc.richtext.CodeArea;

import java.util.HashSet;
import java.util.Set;

public abstract class FoldParser {

    protected FoldRegions foldRegions;
    protected final Set<Integer> foldedParagraphs;

    public FoldParser(CodeArea codeArea) {
        foldRegions = new FoldRegions(codeArea);
        foldedParagraphs = new HashSet<>();
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
