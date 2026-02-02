package com.campaignworkbench.campaignrenderer;

import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import de.jensd.fx.glyphs.fontawesome.utils.FontAwesomeIconFactory;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;

public class WorkspaceExplorerItem {

    public static class IconText {
        public final FontAwesomeIcon icon;
        public final String text;
        public final String iconSize;
        public final int spacing;
        public final Color iconColor;

        public IconText(FontAwesomeIcon icon, String text, String iconSize, int spacing, Color iconColor) {
            this.icon = icon;
            this.text = text;
            this.iconSize = iconSize;
            this.spacing = spacing;
            this.iconColor = iconColor;
        }
    }

    // TreeItem factory for plain text
    public static TreeItem<Object> createTreeItem(String text) {
        return new TreeItem<>(text);
    }

    public static TreeItem<Object> createTreeItem(WorkspaceFile workspaceFile) {
        return new TreeItem<>(workspaceFile);
    }

    // TreeItem factory for icon+text
    public static TreeItem<Object> createTreeItem(FontAwesomeIcon icon, String text, String iconSize, int spacing, Color iconColor) {
        return new TreeItem<>(new IconText(icon, text, iconSize, spacing, iconColor));
    }

    // Cell factory for mixed content
    public static void enableMixedContent(TreeView<Object> treeView, int paddingTB, int paddingLR) {
        treeView.setCellFactory(tv -> new TreeCell<>() {
            @Override
            protected void updateItem(Object item, boolean empty) {
                super.updateItem(item, empty);

                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else if (item instanceof IconText iconText) {
                    // Create new HBox for this cell — do NOT reuse previous Node
                    Text iconPart = FontAwesomeIconFactory.get().createIcon(iconText.icon, iconText.iconSize);
                    iconPart.setFill(iconText.iconColor);
                    Text labelPart = new Text(iconText.text);
                    HBox container = new HBox(iconText.spacing, iconPart, labelPart);

                    setText(null);
                    setGraphic(container);
                } else if (item instanceof WorkspaceFile workspaceFileItem) {
                    setText(workspaceFileItem.getBaseFileName());
                } else {
                    // Plain text
                    setText(item.toString());
                    setGraphic(null);
                }

                setStyle("-fx-padding: " + paddingTB + " " + paddingLR + " " + paddingTB + " " + paddingLR + ";");
            }
        });
    }
}
