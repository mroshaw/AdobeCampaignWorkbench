package com.campaignworkbench.ide.editor.richtextfx;

import com.campaignworkbench.ide.IDETheme;
import org.fxmisc.richtext.model.StyleSpans;

import java.util.Collection;

public interface ISyntaxStyler {
    public StyleSpans<Collection<String>> style(String text);

    public String getStyleSheet(IDETheme theme);
}
