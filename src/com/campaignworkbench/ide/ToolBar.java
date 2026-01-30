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
    private final Button setXmlButton;
    private final Button clearXmlButton;

    /**
     * Constructor
     * @param openWorkspaceHandler - action to take when open workspace button is clicked
     * @param setContextXmlHandler - action to take when set XML context button is clicked
     * @param clearContextXmlHandler - action to take when Clear XML context button is clicked
     * @param runHandler - action to take when run button is clicked
     */
    public ToolBar(
            EventHandler<ActionEvent> openWorkspaceHandler,
            EventHandler<ActionEvent> setContextXmlHandler,
            EventHandler<ActionEvent> clearContextXmlHandler,
            EventHandler<ActionEvent> runHandler
    ) {
        // Open Template button
        Button openWorkspaceButton = new Button("Open Workspace");
        openWorkspaceButton.setTooltip(new Tooltip("Open Workspace"));
        Label newFileLabel = new Label("\uD83D\uDCC1");
        newFileLabel.setStyle("-fx-text-fill: yellow; -fx-font-size: 18px;");

        openWorkspaceButton.setGraphic(newFileLabel);
        openWorkspaceButton.setOnAction(openWorkspaceHandler);

        // Set XML Context button
        setXmlButton = new Button("Set Context XML");
        setXmlButton.setTooltip(new Tooltip("Set Context XML"));
        Label setContextLabel = new Label("\uD83D\uDD27");
        setContextLabel.setStyle("-fx-text-fill: green; -fx-font-size: 18px;");
        setXmlButton.setGraphic(setContextLabel);
        setXmlButton.setOnAction(setContextXmlHandler);

        // Clear XML Context button
        clearXmlButton = new Button("Clear Context XML");
        clearXmlButton.setTooltip(new Tooltip("Clear Context XML"));
        Label clearXmlLabel = new Label("✖");
        clearXmlLabel.setStyle("-fx-text-fill: red; -fx-font-size: 18px;");
        clearXmlButton.setOnAction(clearContextXmlHandler);
        clearXmlButton.setGraphic(clearXmlLabel);

        // Run Template button
        runButton = new Button("Run Template");
        runButton.setTooltip(new Tooltip("Run Template"));
        Label arrowLabel = new Label("▶");
        arrowLabel.setStyle("-fx-text-fill: green; -fx-font-size: 18px;");
        runButton.setGraphic(arrowLabel);
        runButton.setDisable(true);
        runButton.setOnAction(runHandler);

        toolBar = new HBox(
                10,
                openWorkspaceButton,
                setXmlButton,
                clearXmlButton,
                runButton
        );
        toolBar.setAlignment(Pos.CENTER_LEFT);
        toolBar.getStyleClass().add("tool-bar");
    }

    /**
     * @param state enabled state of the "Set Context XML" button
     */
    public void setContextXmlState(boolean state) {
        setXmlButton.setDisable(!state);
    }

    public void setClearContextXmlState(boolean state) {
        clearXmlButton.setDisable(!state);
    }

    /**
     * @param state true or false state of the run button
     */
    public void setRunButtonState(boolean state) {
        runButton.setDisable(!state);
    }

    @Override
    public Node getNode() {
        return toolBar;
    }
}
