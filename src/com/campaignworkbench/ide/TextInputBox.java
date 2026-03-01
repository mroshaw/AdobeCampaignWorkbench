package com.campaignworkbench.ide;
import javafx.scene.control.TextInputDialog;
import javafx.stage.Window;

import java.util.Optional;

public class TextInputBox {
    public static Optional<String> show(Window owner, String title, String headerText, String contentText) {

        TextInputDialog dialog = new TextInputDialog();

        dialog.initOwner(owner);
        dialog.setTitle(title);
        dialog.setHeaderText(headerText);
        dialog.setContentText(contentText);

        // Optional: prevent empty submissions
        dialog.getEditor().textProperty().addListener((obs, oldVal, newVal) -> {
            dialog.getDialogPane().lookupButton(javafx.scene.control.ButtonType.OK)
                    .setDisable(newVal == null || newVal.trim().isEmpty());
        });

        return dialog.showAndWait()
                .map(String::trim)
                .filter(s -> !s.isEmpty());
    }
}
