package com.campaignworkbench.ide;

import com.campaignworkbench.campaignrenderer.WorkspaceFile;
import com.campaignworkbench.campaignrenderer.WorkspaceFileType;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;
import org.controlsfx.glyphfont.FontAwesome;
import org.controlsfx.glyphfont.Glyph;

/**
 * Class to represent items in the Workspace Explorer TreeeView. Adds support for mixed content (text and icon)
 * and workspace files.
 */
public class WorkspaceExplorerItem {

    public static class HeaderTreeItem {
        public final FontAwesome.Glyph icon;
        public final String text;
        public final String iconSize;
        public final int spacing;
        public final String iconStyleClass;
        public final WorkspaceFileType fileType;

        public HeaderTreeItem(FontAwesome.Glyph icon, String text, String iconSize, int spacing, String iconStyleClass, WorkspaceFileType fileType) {
            this.icon = icon;
            this.text = text;
            this.iconSize = iconSize;
            this.spacing = spacing;
            this.iconStyleClass = iconStyleClass;
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
    public static TreeItem<Object> createHeaderTreeItem(FontAwesome.Glyph icon, String text, String iconSize, int spacing, String iconStyleClass, WorkspaceFileType fileType) {
        return new TreeItem<>(new HeaderTreeItem(icon, text, iconSize, spacing, iconStyleClass, fileType));
    }

    // Cell factory for mixed content
    public static void enableMixedContent(TreeView<Object> treeView, int paddingTB, int paddingLR) {
        treeView.setCellFactory(_ -> new TreeCell<>() {
            @Override
            protected void updateItem(Object item, boolean empty) {
                super.updateItem(item, empty);

                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else if (item instanceof HeaderTreeItem iconText) {
                    Glyph glyph = new Glyph("FontAwesome", iconText.icon).sizeFactor(1);
                    glyph.getStyleClass().add(iconText.iconStyleClass);
                    Text labelPart = new Text(iconText.text);
                    HBox container = new HBox(iconText.spacing, glyph, labelPart);

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
