package com.campaignworkbench.ide;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;

/**
 * Provides a menu bar for use in the IDE User Interface
 */
public class MainMenuBar implements IJavaFxNode {

    /**
     * The menu bar component
     */
    MenuBar menuBar;

    /**
     * Constructor
     * @param openWorkspaceHandler action to run when open workspace menu is selected
     * @param openTemplateFileHandler action to run when open template menu is selected
     * @param openModuleFileHandler action to run when open module menu is selected
     * @param openBlockFileHandler action to run when open block menu is selected
     * @param openXMLFileHandler action to run when open XML menu is selected
     * @param saveCurrentFileHandler action to run when save current file menu is selected
     * @param applyLightThemeHandler action to run when apply light theme menu is selected
     * @param applyDarkThemeHandler action to run when apply dark theme menu is selected
     */
    public MainMenuBar(
            EventHandler<ActionEvent> openWorkspaceHandler,
            EventHandler<ActionEvent> openTemplateFileHandler,
            EventHandler<ActionEvent> openModuleFileHandler,
            EventHandler<ActionEvent> openBlockFileHandler,
            EventHandler<ActionEvent> openXMLFileHandler,
            EventHandler<ActionEvent> saveCurrentFileHandler,

            EventHandler<ActionEvent> applyLightThemeHandler,
            EventHandler<ActionEvent> applyDarkThemeHandler
    ) {
        menuBar = new MenuBar();

        // --- File Menu ---
        Menu fileMenu = new Menu("File");

        // Open submenu
        Menu openSub = new Menu("Open");
        MenuItem openWorkspace = new MenuItem("Workspace");
        MenuItem openTemplate = new MenuItem("Template");
        MenuItem openModule = new MenuItem("Module");
        MenuItem openBlock = new MenuItem("Block");
        MenuItem openXmlContext = new MenuItem("Context XML");

        openWorkspace.setOnAction(openWorkspaceHandler);
        openTemplate.setOnAction(openTemplateFileHandler);
        openModule.setOnAction(openModuleFileHandler);
        openBlock.setOnAction(openBlockFileHandler);
        openXmlContext.setOnAction(openXMLFileHandler);

        openSub.getItems().addAll(openWorkspace, openTemplate, openModule, openBlock, openXmlContext);

        // New submenu
        Menu newSub = new Menu("New");
        MenuItem newWorkspace = new MenuItem("Workspace");
        MenuItem newTemplate = new MenuItem("Template");
        MenuItem newModule = new MenuItem("Module");
        MenuItem newBlock = new MenuItem("Block");
        MenuItem newXmlContext = new MenuItem("Context XML");

        newWorkspace.setOnAction(event -> dummyHandler());
        newTemplate.setOnAction(event -> dummyHandler());
        newModule.setOnAction(event -> dummyHandler());
        newBlock.setOnAction(event -> dummyHandler());
        newXmlContext.setOnAction(event -> dummyHandler());

        newSub.getItems().addAll(newWorkspace, newTemplate, newModule, newBlock, newXmlContext);

        // Save items
        MenuItem saveCurrent = new MenuItem("Save Current");
        saveCurrent.setAccelerator(
                new KeyCodeCombination(KeyCode.S, KeyCombination.CONTROL_DOWN)
        );
        MenuItem saveCurrentAs = new MenuItem("Save Current As");
        MenuItem exitItem = new MenuItem("Exit");

        saveCurrent.setOnAction(saveCurrentFileHandler);
        saveCurrentAs.setOnAction(event -> dummyHandler());
        exitItem.setOnAction(event -> Platform.exit());

        fileMenu.getItems().addAll(openSub, newSub, saveCurrent, saveCurrentAs, new SeparatorMenuItem(), exitItem);

        // --- Help Menu ---
        Menu helpMenu = new Menu("Help");
        MenuItem aboutItem = new MenuItem("About");
        aboutItem.setOnAction(event -> dummyHandler());
        helpMenu.getItems().add(aboutItem);

        Menu viewMenu = new Menu("View");
        MenuItem darkThemeItem = new MenuItem("Dark Theme");
        darkThemeItem.setOnAction(applyDarkThemeHandler);
        viewMenu.getItems().add(darkThemeItem);

        MenuItem lightThemeItem = new MenuItem("Light Theme");
        lightThemeItem.setOnAction(applyLightThemeHandler);
        viewMenu.getItems().add(lightThemeItem);


        // --- Add menus to menu bar ---
        menuBar.getMenus().addAll(fileMenu, viewMenu, helpMenu);
    }

    /**
     * Temp dummy handle for new menu items/testing
     */
    private void dummyHandler() {
    }

    @Override
    public Node getNode() {
        return menuBar;
    }
}
