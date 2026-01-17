package com.campaignworkbench.ide.editor;

import com.campaignworkbench.ide.IThemeable;
import com.campaignworkbench.ide.IDETheme;
import com.campaignworkbench.ide.ThemeManager;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;

public class ToolBar implements IThemeable, IJavaFxNode {

    HBox toolBar;
    Button runButton;
    Label xmlContextLabel;

    Button openTemplateButton;
    Button openBlockButton;
    Button openXmlButton;
    Button setXmlButton;

    public ToolBar(
            EventHandler<ActionEvent> openTemplateFileHandler,
            EventHandler<ActionEvent> openBlockFileHandler,
            EventHandler<ActionEvent> openXMLFileHandler,
            EventHandler<ActionEvent> setXmlContextHandler,
            EventHandler<ActionEvent> runHandler
    ) {
        // --- Top toolbar ---
        openTemplateButton = new Button("Open Template");
        openBlockButton = new Button("Open Block");
        openXmlButton = new Button("Open XML");
        setXmlButton = new Button("Set XML Context");

        xmlContextLabel = new Label("No XML context set");

        runButton = new Button();
        runButton.setTooltip(new Tooltip("Run Template"));
        Label arrowLabel = new Label("▶");
        arrowLabel.setStyle("-fx-text-fill: green; -fx-font-size: 18px;");
        runButton.setGraphic(arrowLabel);
        runButton.setDisable(true);

        openTemplateButton.setOnAction(openTemplateFileHandler);
        openBlockButton.setOnAction(openBlockFileHandler);
        openXmlButton.setOnAction(openXMLFileHandler);
        setXmlButton.setOnAction(setXmlContextHandler);
        runButton.setOnAction(runHandler);

        toolBar = new HBox(
                10,
                openTemplateButton,
                // openBlockButton,
                // openXmlButton,
                setXmlButton,
                xmlContextLabel,
                runButton
        );
        toolBar.setAlignment(Pos.CENTER_LEFT);
        toolBar.getStyleClass().add("tool-bar");
        ThemeManager.register(this);
    }

    public void setXmlContextLabel(String labelText) {
        xmlContextLabel.setText(labelText);
    }

    public void setRunButtonState(boolean state) {
        runButton.setDisable(state);
    }

    @Override
    public void applyTheme(IDETheme theme) {
    }

    @Override
    public Node getNode() {
        return toolBar;
    }
}
