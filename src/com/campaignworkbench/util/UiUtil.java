package com.campaignworkbench.util;

import org.controlsfx.glyphfont.FontAwesome;
import org.controlsfx.glyphfont.Glyph;

import javafx.event.EventHandler;
import javafx.scene.control.Button;
import javafx.scene.control.Tooltip;

public class UiUtil {

    public static Button createButton(String buttonText, String toolTipText, FontAwesome.Glyph icon, String styleClass, Integer sizeFactor, boolean defaultState, EventHandler eventHandler) {
        Glyph glyph = new Glyph("FontAwesome", icon).sizeFactor(sizeFactor);
        glyph.getStyleClass().add(styleClass);
        Button newButton = new Button(buttonText, glyph);
        newButton.setTooltip(new Tooltip(toolTipText));
        newButton.setOnAction(eventHandler);
        newButton.setDisable(!defaultState);
        return newButton;
    }
}
