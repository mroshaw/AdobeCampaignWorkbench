package com.campaignworkbench.ide;

import javafx.application.Platform;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;

/**
 * User interface control to provided a logging console
 */
public class LogPanel implements IJavaFxNode {

    private final TextArea logArea;
    VBox logBox;

    /**
     * Constructor
     */
    public LogPanel() {
        // --- Full-width log pane ---
        Label logLabel = new Label("Logs");
        logArea = new TextArea();
        logArea.setEditable(false);
        logArea.setWrapText(true);
        logArea.setCursor(Cursor.TEXT); // full-width log pane
        logArea.setFont(Font.font("Source Code Pro", 14));

        logBox = new VBox(5, logLabel, logArea);
        VBox.setVgrow(logArea, Priority.ALWAYS);
    }

    public Node getNode() {
        return logBox;
    }

    /**
     * Adds a line of content to the log
     * @param msg text containing content of the log line to add
     */
    public void appendLog(String msg) {
        Platform.runLater(() -> logArea.appendText(msg + "\n"));
    }

}
