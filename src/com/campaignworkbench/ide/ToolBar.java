package com.campaignworkbench.ide;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;

/**
 * Implements a button toolbar for use within the IDE User Interface
 */
public class ToolBar implements IJavaFxNode {

    private final HBox toolBar;
    private final Button runButton;
    private final Label xmlContextLabel;

    /**
     * @param openTemplateFileHandler - action to take when open template button is clicked
     * @param setXmlContextHandler - action to take when set XML context button is clicked
     * @param clearXmlContextHandler - action to take when Clear XML context button is clicked
     * @param runHandler - action to take when run button is clicked
     */
    public ToolBar(
            EventHandler<ActionEvent> openTemplateFileHandler,
            EventHandler<ActionEvent> setXmlContextHandler,
            EventHandler<ActionEvent> clearXmlContextHandler,
            EventHandler<ActionEvent> runHandler
    ) {
        // Open Template button
        Button openTemplateButton = new Button("Open Template");
        openTemplateButton.setTooltip(new Tooltip("Open Template"));
        Label newFileLabel = new Label("\uD83D\uDCC4");
        newFileLabel.setStyle("-fx-font-size: 18px;");
        openTemplateButton.setGraphic(newFileLabel);
        openTemplateButton.setOnAction(openTemplateFileHandler);

        // Set XML Context button
        Button setXmlButton = new Button("Set XML Context");
        setXmlButton.setTooltip(new Tooltip("Set XML Context"));
        Label setContextLabel = new Label("\uD83D\uDD27");
        setXmlButton.setGraphic(setContextLabel);
        setXmlButton.setOnAction(setXmlContextHandler);

        // Clear XML Context button
        Button clearXmlButton = new Button("Clear XML Context");
        clearXmlButton.setTooltip(new Tooltip("Clear XML Context"));
        Label clearXmlLabel = new Label("✖");
        clearXmlLabel.setStyle("-fx-text-fill: red; -fx-font-size: 18px;");
        clearXmlButton.setOnAction(clearXmlContextHandler);
        clearXmlButton.setGraphic(clearXmlLabel);

        // Run Template button
        runButton = new Button("Run Template");
        runButton.setTooltip(new Tooltip("Run Template"));
        Label arrowLabel = new Label("▶");
        arrowLabel.setStyle("-fx-text-fill: green; -fx-font-size: 18px;");
        runButton.setGraphic(arrowLabel);
        runButton.setDisable(true);
        runButton.setOnAction(runHandler);

        Label xmlContextTitleLabel = new Label("XML Context: ");
        xmlContextLabel = new Label();
        clearXmlContextLabel();

        toolBar = new HBox(
                10,
                openTemplateButton,
                setXmlButton,
                clearXmlButton,
                runButton,
                xmlContextTitleLabel,
                xmlContextLabel
        );
        toolBar.setAlignment(Pos.CENTER_LEFT);
        toolBar.getStyleClass().add("tool-bar");
    }

    /**
     * @param labelText file name of the XML file selected as context
     */
    public void setXmlContextLabel(String labelText) {
        xmlContextLabel.setText(labelText);
        xmlContextLabel.setStyle("-fx-text-fill: green;");
    }

    /**
     * Resets the XML Context label to "none set"
     */
    public void clearXmlContextLabel() {
        xmlContextLabel.setText("No XML context set");
        xmlContextLabel.setStyle("-fx-text-fill: red;");
    }

    /**
     * @param state true or false state of the run button
     */
    public void setRunButtonState(boolean state) {
        runButton.setDisable(state);
    }

    @Override
    public Node getNode() {
        return toolBar;
    }
}
