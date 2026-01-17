package com.campaignworkbench.ide.editor;

import com.campaignworkbench.ide.IThemeable;
import com.campaignworkbench.ide.IDETheme;
import com.campaignworkbench.ide.ThemeManager;
import javafx.application.Platform;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;

public class LogPanel implements IThemeable, IJavaFxNode  {

    private TextArea logArea;
    private Label logLabel;
    VBox logBox;

    public LogPanel() {
        // --- Full-width log pane ---
        logLabel = new Label("Logs");
        logArea = new TextArea();
        logArea.setEditable(false);
        logArea.setWrapText(true);
        logArea.setCursor(Cursor.TEXT); // full-width log pane
        logArea.setFont(Font.font("Source Code Pro", 14));

        logBox = new VBox(5, logLabel, logArea);
        VBox.setVgrow(logArea, Priority.ALWAYS);
        ThemeManager.register(this);
    }

    public Node getNode() {
        return logBox;
    }

    @Override
    public void applyTheme(IDETheme theme) {
        switch (theme) {
            case DARK:
                logBox.setStyle("-fx-background-color: #2b2b2b;");
                logLabel.setStyle("-fx-text-fill: white;");
                logArea.setStyle(
                        "-fx-control-inner-background: #1e1e1e;" +
                                "-fx-text-fill: white;" +
                                "-fx-highlight-fill: #555555;" +
                                "-fx-highlight-text-fill: white;"
                );
                break;

            case LIGHT:
            default:
                logBox.setStyle("-fx-background-color: #ececec;");
                logLabel.setStyle("-fx-text-fill: black;");
                logArea.setStyle(
                        "-fx-control-inner-background: white;" +
                                "-fx-text-fill: black;" +
                                "-fx-highlight-fill: #cce6ff;" +
                                "-fx-highlight-text-fill: black;"
                );
                break;
        }
    }

    public void appendLog(String msg) {
        Platform.runLater(() -> logArea.appendText(msg + "\n"));
    }

}
