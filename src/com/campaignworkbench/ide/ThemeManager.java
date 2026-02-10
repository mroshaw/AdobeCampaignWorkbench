package com.campaignworkbench.ide;

import com.campaignworkbench.ide.editor.ICodeEditor;

import java.util.ArrayList;
import java.util.List;

/**
 * Manages the application-wide theme and handles registration of editors for theme updates
 */
public final class ThemeManager {

    private static IDETheme currentTheme = IDETheme.DARK;
    private static final List<IThemeable> themeables = new ArrayList<>();

    private ThemeManager() {}

    /**
     * Registers an editor to receive theme updates
     * @param editor the editor to register
     */
    public static void register(IThemeable editor) {
        themeables.add(editor);
        editor.applyTheme(currentTheme);
    }

    /**
     * Re-applies the current theme to all registered components
     */
    public static void applyCurrentTheme() {
        setTheme(currentTheme);
    }

    /**
     * Sets a new application-wide theme
     * @param theme the theme to apply
     */
    public static void setTheme(IDETheme theme) {
        currentTheme = theme;

        CampaignWorkbenchIDE.setTheme(theme);
        for (IThemeable themeable : themeables) {
            themeable.applyTheme(theme);
        }
    }
}
