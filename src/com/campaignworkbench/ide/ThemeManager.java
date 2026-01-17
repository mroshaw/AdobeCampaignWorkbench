package com.campaignworkbench.ide;

import javafx.scene.Scene;

import java.util.ArrayList;
import java.util.List;

public final class ThemeManager {

    private static IDETheme currentTheme = IDETheme.DARK;
    private static final List<IThemeable> themeables = new ArrayList<>();

    private ThemeManager() {}
    private static Scene mainScene;

    public static void register(IThemeable component) {
        themeables.add(component);
        component.applyTheme(currentTheme); // apply current theme immediately
    }

    public static void setTheme(IDETheme theme, Scene scene) {
        currentTheme = theme;
        mainScene = scene;

        // Apply scene-wide CSS
        mainScene.getStylesheets().clear();
        String cssFile = (theme == IDETheme.DARK) ? "/dark-theme.css" : "/light-theme.css";
        mainScene.getStylesheets().add(ThemeManager.class.getResource(cssFile).toExternalForm());

        // Apply to registered components
        for (IThemeable t : themeables) {
            t.applyTheme(theme);
        }
    }

    public static IDETheme getCurrentTheme() {
        return currentTheme;
    }
}
