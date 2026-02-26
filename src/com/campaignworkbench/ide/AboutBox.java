package com.campaignworkbench.ide;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Window;

import java.time.Year;

public class AboutBox {

    private static final String appName = "Campaign Workbench";
    private static final String description = "Powerful IDE to build and test Campaign Classic template code";
    private static final String version = Version.VERSION;
    private static final String copyright = "©" + Year.now().getValue() + " Specsavers";
    private static final Image icon = new Image(CampaignWorkbenchIDE.class.getResourceAsStream("/images/icon.png"));

    public static void show(Window owner) {

        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("About " + appName);
        dialog.initOwner(owner);
        dialog.setResizable(false);

        dialog.getDialogPane().getButtonTypes().add(ButtonType.OK);

        VBox content = new VBox(10);
        content.setPadding(new Insets(20));
        content.setAlignment(Pos.TOP_LEFT);

        // Top section (icon + title)
        HBox header = new HBox(15);
        header.setAlignment(Pos.CENTER_LEFT);

        ImageView imageView = new ImageView(icon);
        imageView.setFitWidth(64);
        imageView.setFitHeight(64);
        header.getChildren().add(imageView);

        VBox titleBox = new VBox(5);
        Label nameLabel = new Label(appName);
        nameLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        Label versionLabel = new Label("Version " + version);
        versionLabel.setStyle("-fx-opacity: 0.75;");

        titleBox.getChildren().addAll(nameLabel, versionLabel);
        header.getChildren().add(titleBox);

        // Description
        Label descriptionLabel = new Label(description);
        descriptionLabel.setWrapText(true);

        // Copyright
        Label copyrightLabel = new Label(copyright);
        copyrightLabel.setStyle("-fx-opacity: 0.65;");

        content.getChildren().addAll(header, descriptionLabel, copyrightLabel);

        dialog.getDialogPane().setContent(content);

        dialog.showAndWait();
    }
}
