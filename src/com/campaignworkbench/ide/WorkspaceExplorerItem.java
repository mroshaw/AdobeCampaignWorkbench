package com.campaignworkbench.ide;

import com.campaignworkbench.campaignrenderer.WorkspaceFile;
import com.campaignworkbench.campaignrenderer.WorkspaceFileType;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import de.jensd.fx.glyphs.fontawesome.utils.FontAwesomeIconFactory;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;

public class WorkspaceExplorerItem {

    public static class HeaderTreeItem {
        public final FontAwesomeIcon icon;
        public final String text;
        public final String iconSize;
        public final int spacing;
        public final Color iconColor;
        public final WorkspaceFileType fileType;

        public HeaderTreeItem(FontAwesomeIcon icon, String text, String iconSize, int spacing, Color iconColor, WorkspaceFileType fileType) {
            this.icon = icon;
            this.text = text;
            this.iconSize = iconSize;
            this.spacing = spacing;
            this.iconColor = iconColor;
            this.fileType = fileType;
        }
    }

    public static class WorkspaceFileTreeItem {

        public final WorkspaceFile workspaceFile;

        public WorkspaceFileTreeItem(WorkspaceFile workspaceFile) {
            this.workspaceFile = workspaceFile;
        }
    }

    public static class ContextTreeItem extends WorkspaceFileTreeItem {
        public final String contextLabel;

        public ContextTreeItem(WorkspaceFile workspaceFile, String contextLabel ) {
            super(workspaceFile);
            this.contextLabel = contextLabel;
        }
    }

    // TreeItem factory for plain text
    public static TreeItem<Object> createTextTreeItem(String text) {
        return new TreeItem<>(text);
    }

    public static TreeItem<Object> createWorkspaceFileTreeItem(WorkspaceFile workspaceFile) {
        WorkspaceFileTreeItem newTreeItem = new WorkspaceFileTreeItem(workspaceFile);
        return new TreeItem<>(newTreeItem);
    }

    public static TreeItem<Object> createContextFileTreeItem(WorkspaceFile workspaceFile, String contextLabel) {
        ContextTreeItem newTreeItem = new ContextTreeItem(workspaceFile, contextLabel);
        return new TreeItem<>(newTreeItem);
    }

    // TreeItem factory for icon+text
    public static TreeItem<Object> createHeaderTreeItem(FontAwesomeIcon icon, String text, String iconSize, int spacing, Color iconColor, WorkspaceFileType fileType) {
        return new TreeItem<>(new HeaderTreeItem(icon, text, iconSize, spacing, iconColor, fileType));
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
                } else if (item instanceof HeaderTreeItem iconText) {
                    // Create new HBox for this cell — do NOT reuse previous Node
                    Text iconPart = FontAwesomeIconFactory.get().createIcon(iconText.icon, iconText.iconSize);
                    iconPart.setFill(iconText.iconColor);
                    Text labelPart = new Text(iconText.text);
                    HBox container = new HBox(iconText.spacing, iconPart, labelPart);

                    setText(null);
                    setGraphic(container);
                } else if (item instanceof ContextTreeItem contextTreeItem) {
                    if(contextTreeItem.workspaceFile != null )
                    {
                        setText(contextTreeItem.contextLabel + ": " + contextTreeItem.workspaceFile.getBaseFileName());
                    } else
                    {
                        setText(contextTreeItem.contextLabel + ": (NOT SET)");
                    }

                } else if (item instanceof WorkspaceFileTreeItem workspaceFileTreeItem) {
                    setText(workspaceFileTreeItem.workspaceFile.getBaseFileName());
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
