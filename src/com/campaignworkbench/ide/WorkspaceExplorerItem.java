package com.campaignworkbench.ide;

import com.campaignworkbench.campaignrenderer.WorkspaceFile;
import com.campaignworkbench.campaignrenderer.WorkspaceFileType;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;
import org.controlsfx.glyphfont.FontAwesome;
import org.controlsfx.glyphfont.Glyph;

import java.util.function.Consumer;

/**
 * Class to represent items in the Workspace Explorer TreeView.
 * Adds support for mixed content (text and icon) and workspace files.
 * Revised for TreeView virtualization safety.
 */
public class WorkspaceExplorerItem {

    public static class HeaderTreeItem {
        public final FontAwesome.Glyph icon;
        public final String text;
        public final String iconSize;
        public final int spacing;
        public final String iconStyleClass;
        public final WorkspaceFileType fileType;

        // Cached graphic and context menu
        public final HBox graphic;
        public final ContextMenu contextMenu;

        // Context menu handlers
        public final Consumer<WorkspaceFileType> addNewHandler;
        public final Consumer<WorkspaceFileType> addExistingHandler;


        public HeaderTreeItem(FontAwesome.Glyph icon, String text, String iconSize, int spacing, String iconStyleClass, WorkspaceFileType fileType, Consumer<WorkspaceFileType> addNewHandler, Consumer<WorkspaceFileType> addExistingHandler) {
            this.icon = icon;
            this.text = text;
            this.iconSize = iconSize;
            this.spacing = spacing;
            this.iconStyleClass = iconStyleClass;
            this.fileType = fileType;
            this.addNewHandler = addNewHandler;
            this.addExistingHandler = addExistingHandler;

            // Create graphic once
            Glyph glyph = new Glyph("FontAwesome", icon).sizeFactor(1);
            glyph.getStyleClass().add(iconStyleClass);
            Text labelPart = new Text(text);
            this.graphic = new HBox(spacing, glyph, labelPart);

            // Create ContextMenu once
            this.contextMenu = new ContextMenu();
            if (fileType != null) {
                MenuItem addNew = new MenuItem("Add new...");
                addNew.setOnAction(e -> addNewHandler(this));
                this.contextMenu.getItems().add(addNew);

                MenuItem addExisting = new MenuItem("Add existing...");
                addExisting.setOnAction(e -> addExistingHandler(this));
                this.contextMenu.getItems().add(addExisting);
            }
        }
    }

    public static class WorkspaceFileTreeItem {

        public final WorkspaceFile workspaceFile;
        public final Consumer<String> getFileNameContextHandler;

        public WorkspaceFileTreeItem(WorkspaceFile workspaceFile, Consumer<String> getFileNameContextHandler) {
            this.workspaceFile = workspaceFile;
            this.getFileNameContextHandler = getFileNameContextHandler;
        }
    }

    public static class ContextTreeItem extends WorkspaceFileTreeItem {
        public final String contextLabel;

        public ContextTreeItem(WorkspaceFile workspaceFile, String contextLabel) {
            super(workspaceFile, null);
            this.contextLabel = contextLabel;
        }
    }

    // TreeItem factories
    public static TreeItem<Object> createTextTreeItem(String text) {
        return new TreeItem<>(text);
    }

    public static TreeItem<Object> createWorkspaceFileTreeItem(WorkspaceFile workspaceFile, Consumer<String> getFileNameContextHandler) {
        WorkspaceFileTreeItem newTreeItem = new WorkspaceFileTreeItem(workspaceFile, getFileNameContextHandler);
        return new TreeItem<>(newTreeItem);
    }

    public static TreeItem<Object> createContextFileTreeItem(WorkspaceFile workspaceFile, String contextLabel) {
        ContextTreeItem newTreeItem = new ContextTreeItem(workspaceFile, contextLabel);
        return new TreeItem<>(newTreeItem);
    }

    public static TreeItem<Object> createHeaderTreeItem(FontAwesome.Glyph icon, String text, String iconSize, int spacing, String iconStyleClass, WorkspaceFileType fileType, Consumer<WorkspaceFileType> addNewHandler, Consumer<WorkspaceFileType> addExistingHandler) {
        HeaderTreeItem header = new HeaderTreeItem(icon, text, iconSize, spacing, iconStyleClass, fileType, addNewHandler,  addExistingHandler);
        return new TreeItem<>(header);
    }

    // Virtualization-safe CellFactory
    public static void enableMixedContent(TreeView<Object> treeView, int paddingTB, int paddingLR) {
        treeView.setCellFactory(tv -> new TreeCell<>() {
            @Override
            protected void updateItem(Object item, boolean empty) {
                super.updateItem(item, empty);

                // Reset cell state first (important for recycled cells)
                setText(null);
                setGraphic(null);
                setContextMenu(null);

                if (empty || item == null) {
                    return;
                }

                if (item instanceof HeaderTreeItem iconText) {
                    setGraphic(iconText.graphic);
                    setContextMenu(iconText.contextMenu);

                } else if (item instanceof ContextTreeItem contextTreeItem) {
                    if (contextTreeItem.workspaceFile != null) {
                        setText(contextTreeItem.contextLabel + ": " + contextTreeItem.workspaceFile.getBaseFileName());
                    } else {
                        setText(contextTreeItem.contextLabel + ": (NOT SET)");
                    }
                    setContextMenu(null);

                } else if (item instanceof WorkspaceFileTreeItem workspaceFileTreeItem) {
                    setText(workspaceFileTreeItem.workspaceFile.getBaseFileName());

                    WorkspaceFileType fileType = workspaceFileTreeItem.workspaceFile.getWorkspaceFileType();
                    if (fileType == WorkspaceFileType.BLOCK || fileType == WorkspaceFileType.MODULE) {
                        ContextMenu menu = new ContextMenu();
                        MenuItem insertIntoCode = new MenuItem("Insert into code...");
                        insertIntoCode.setOnAction(e -> insertIntoCodeHandler(workspaceFileTreeItem));
                        menu.getItems().add(insertIntoCode);
                        setContextMenu(menu);
                    }

                } else {
                    setText(item.toString());
                    setGraphic(null);
                }

                setStyle("-fx-padding: " + paddingTB + " " + paddingLR + " " + paddingTB + " " + paddingLR + ";");
            }
        });
    }

    // Handlers remain the same
    private static void addNewHandler(HeaderTreeItem headerTreeItem) {
        headerTreeItem.addNewHandler.accept(headerTreeItem.fileType);
    }

    private static void addExistingHandler(HeaderTreeItem headerTreeItem) {
        headerTreeItem.addExistingHandler.accept(headerTreeItem.fileType);
    }


    private static void insertIntoCodeHandler(WorkspaceFileTreeItem workspaceFileTreeItem) {
        String includeType = workspaceFileTreeItem.workspaceFile.getWorkspaceFileType() == WorkspaceFileType.BLOCK ? "view" : "module";
        String fileName = workspaceFileTreeItem.workspaceFile.getBaseFileName();
        String text = "<%@ include " + includeType + "='" + fileName + "' %>";
        workspaceFileTreeItem.getFileNameContextHandler.accept(text);
    }

}