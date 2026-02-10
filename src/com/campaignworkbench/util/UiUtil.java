package com.campaignworkbench.util;

import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import de.jensd.fx.glyphs.fontawesome.utils.FontAwesomeIconFactory;
import javafx.event.EventHandler;
import javafx.scene.control.Button;
import javafx.scene.control.Tooltip;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;

public class UiUtil {
    public static Button createButton(String buttonText, String toolTipText, FontAwesomeIcon icon, Color fillColor, String iconSize, boolean defaultState, EventHandler eventHandler) {
        Button newButton = new Button();
        Text buttonIcon = FontAwesomeIconFactory.get().createIcon(icon, iconSize);
        buttonIcon.setFill(fillColor);
        newButton.setTooltip(new Tooltip(toolTipText));
        newButton.setGraphic(buttonIcon);
        newButton.setOnAction(eventHandler);
        newButton.setDisable(!defaultState);

        return newButton;
    }
}
