package com.campaignworkbench.ide;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.stage.Stage;

import java.util.Optional;

public final class YesNoPopupDialog {

    public enum YesNoCancel {
        YES, NO, CANCEL
    }

    private YesNoPopupDialog() {
        // utility class
    }

    public static YesNoCancel show(
            String title,
            String message,
            Stage owner
    ) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.initOwner(owner);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);

        ButtonType yes = new ButtonType("Yes");
        ButtonType no = new ButtonType("No");
        ButtonType cancel = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);

        alert.getButtonTypes().setAll(yes, no, cancel);

        Optional<ButtonType> result = alert.showAndWait();

        if (result.isEmpty() || result.get() == cancel) {
            return YesNoCancel.CANCEL;
        }

        return result.get() == yes
                ? YesNoCancel.YES
                : YesNoCancel.NO;
    }
}
