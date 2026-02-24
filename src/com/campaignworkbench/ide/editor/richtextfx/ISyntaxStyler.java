package com.campaignworkbench.ide.editor.richtextfx;

import com.campaignworkbench.ide.IDETheme;
import org.fxmisc.richtext.model.StyleSpans;
import java.util.Collection;

/**
 * Interface describing a class that generates syntax highlighting for a given text
 */
public interface ISyntaxStyler {
    StyleSpans<Collection<String>> style(String text);

    String getStyleSheet(IDETheme theme);
}
