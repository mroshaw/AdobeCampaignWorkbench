package com.campaignworkbench.ide;

import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import de.jensd.fx.glyphs.fontawesome.utils.FontAwesomeIconFactory;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;

/**
 * Implements a button toolbar for use within the IDE User Interface
 */
public class MainToolBar implements IJavaFxNode {

    private final HBox toolBar;
    private final Button runButton;
    private final Button setDataContextButton;
    private final Button clearDataContextButton;
    private final Button setMessageContextButton;
    private final Button clearMessageContextButton;

    /**
     * Constructor
     * @param openWorkspaceHandler - action to take when open workspace button is clicked
     * @param setDataContextHandler - action to take when set XML context button is clicked
     * @param clearDataContextHandler - action to take when Clear XML context button is clicked
     * @param runHandler - action to take when run button is clicked
     */
    public MainToolBar(
            EventHandler<ActionEvent> openWorkspaceHandler,
            EventHandler<ActionEvent> setDataContextHandler,
            EventHandler<ActionEvent> clearDataContextHandler,
            EventHandler<ActionEvent> setMessageContextHandler,
            EventHandler<ActionEvent> clearMessageContextHandler,
            EventHandler<ActionEvent> runHandler
    ) {
        // Open Template button
        Text openWorkspaceIcon = FontAwesomeIconFactory.get().createIcon(FontAwesomeIcon.FOLDER_OPEN, "24px");
        openWorkspaceIcon.setFill(Color.YELLOW);
        Button openWorkspaceButton = new Button(); //("Open Workspace");
        openWorkspaceButton.setTooltip(new Tooltip("Open Workspace"));
        openWorkspaceButton.setGraphic(openWorkspaceIcon);
        openWorkspaceButton.setOnAction(openWorkspaceHandler);

        // Set Data Context button
        Text setDataContextIcon = FontAwesomeIconFactory.get().createIcon(FontAwesomeIcon.FILE_CODE_ALT, "24px");
        setDataContextIcon.setFill(Color.GREEN);
        setDataContextButton = new Button(); // ("Set Data Context XML");
        setDataContextButton.setTooltip(new Tooltip("Set Data Context"));
        setDataContextButton.setGraphic(setDataContextIcon);
        setDataContextButton.setOnAction(setDataContextHandler);
        setDataContextButton.setDisable(true);

        // Clear Data XML Context button
        Text clearDataContextIcon = FontAwesomeIconFactory.get().createIcon(FontAwesomeIcon.FILE_CODE_ALT, "24px");
        clearDataContextIcon.setFill(Color.RED);
        clearDataContextButton = new Button(); // ("Clear Data Context");
        clearDataContextButton.setTooltip(new Tooltip("Clear Data Context"));
        clearDataContextButton.setGraphic(clearDataContextIcon);
        clearDataContextButton.setOnAction(clearDataContextHandler);
        clearDataContextButton.setDisable(true);

        // Set Message Context button
        Text setMessageContextIcon = FontAwesomeIconFactory.get().createIcon(FontAwesomeIcon.ENVELOPE, "24px");
        setMessageContextIcon.setFill(Color.GREEN);
        setMessageContextButton = new Button(); // ("Set Message Context XML");
        setMessageContextButton.setTooltip(new Tooltip("Set Message Context"));
        setMessageContextButton.setGraphic(setMessageContextIcon);
        setMessageContextButton.setOnAction(setMessageContextHandler);
        setMessageContextButton.setDisable(true);

        // Clear Data XML Context button
        Text clearMessageContextIcon = FontAwesomeIconFactory.get().createIcon(FontAwesomeIcon.ENVELOPE, "24px");
        clearMessageContextIcon.setFill(Color.RED);
        clearMessageContextButton = new Button(); // ("Clear Message XML");
        clearMessageContextButton.setTooltip(new Tooltip("Clear Message Context"));
        clearMessageContextButton.setGraphic(clearMessageContextIcon);
        clearMessageContextButton.setOnAction(clearMessageContextHandler);
        clearMessageContextButton.setDisable(true);

        // Run Template button
        Text runTemplateIcon = FontAwesomeIconFactory.get().createIcon(FontAwesomeIcon.PLAY, "24px");
        runTemplateIcon.setFill(Color.GREEN);
        runButton = new Button(); // ("Run Template");
        runButton.setTooltip(new Tooltip("Run Template"));
        runButton.setGraphic(runTemplateIcon);
        runButton.setDisable(true);
        runButton.setOnAction(runHandler);

        toolBar = new HBox(
                10,
                openWorkspaceButton,
                setDataContextButton,
                clearDataContextButton,
                setMessageContextButton,
                clearMessageContextButton,
                runButton
        );
        toolBar.setAlignment(Pos.CENTER_LEFT);
        toolBar.getStyleClass().add("tool-bar");
    }

    /**
     * @param state enabled state of the "Set Context XML" button
     */
    public void setDataContextState(boolean state) {
        setDataContextButton.setDisable(!state);
    }

    public void setMessageContextState(boolean state) {
        setMessageContextButton.setDisable(!state);
    }

    public void setClearDataContextState(boolean state) {
        clearDataContextButton.setDisable(!state);
    }

    public void setClearMessageContextState(boolean state) {
        clearMessageContextButton.setDisable(!state);
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
