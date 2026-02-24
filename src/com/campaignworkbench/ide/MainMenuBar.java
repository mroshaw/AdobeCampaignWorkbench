package com.campaignworkbench.ide;

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
     * @param openWorkspaceHandler action to run when the 'open workspace' menu is selected
     * @param addTemplateFileHandler action to run when the 'open template' menu is selected
     * @param addModuleFileHandler action to run when the 'open module' menu is selected
     * @param addBlockFileHandler action to run when the 'open block' menu is selected
     * @param addContextFileHandler action to run when the 'open XML' menu is selected
     * @param saveCurrentFileHandler action to run when the 'save current file' menu is selected
     * @param applyLightThemeHandler action to run when the 'apply light theme' menu is selected
     * @param applyDarkThemeHandler action to run when the 'apply dark theme' menu is selected
     */
    public MainMenuBar(
            EventHandler<ActionEvent> newWorkspaceHandler,
            EventHandler<ActionEvent> openWorkspaceHandler,
            EventHandler<ActionEvent> saveWorkspaceHandler,
            EventHandler<ActionEvent> closeWorkspaceHandler,

            EventHandler<ActionEvent> newTemplateFileHandler,
            EventHandler<ActionEvent> newModuleFileHandler,
            EventHandler<ActionEvent> newBlockFileHandler,
            EventHandler<ActionEvent> newContextFileHandler,

            EventHandler<ActionEvent> addTemplateFileHandler,
            EventHandler<ActionEvent> addModuleFileHandler,
            EventHandler<ActionEvent> addBlockFileHandler,
            EventHandler<ActionEvent> addContextFileHandler,

            EventHandler<ActionEvent> saveCurrentFileHandler,
            EventHandler<ActionEvent> saveCurrentAsFileHandler,

            EventHandler<ActionEvent> findHandler,

            EventHandler<ActionEvent> applyLightThemeHandler,
            EventHandler<ActionEvent> applyDarkThemeHandler,

            EventHandler<ActionEvent> helpAboutHandler,
            EventHandler<ActionEvent> exitHandler
    ) {
        menuBar = new MenuBar();

        // File Menu
        Menu fileMenu = new Menu("File");

        // File sub menu
        Menu openMenu = new Menu("Open");
        Menu newMenu = new Menu("New");
        Menu addMenu = new Menu("Add Existing");

        MenuItem openWorkspaceMenu = new MenuItem("Workspace");


        MenuItem saveWorkspaceMenu = new MenuItem("Save Workspace");
        MenuItem closeWorkspaceMenu = new MenuItem("Close Workspace");

        openWorkspaceMenu.setOnAction(openWorkspaceHandler);
        saveWorkspaceMenu.setOnAction(saveWorkspaceHandler);
        closeWorkspaceMenu.setOnAction(closeWorkspaceHandler);

        // New sub menu
        MenuItem newWorkspaceMenu = new MenuItem("Workspace");
        MenuItem newTemplateMenu = new MenuItem("Template");
        MenuItem newModuleMenu = new MenuItem("Module");
        MenuItem newBlockMenu = new MenuItem("Block");
        MenuItem newXmlContextMenu = new MenuItem("Context XML");
        MenuItem exitItem = new MenuItem("Exit");

        newMenu.getItems().addAll(newWorkspaceMenu, new SeparatorMenuItem(), newTemplateMenu, newModuleMenu, newBlockMenu, newXmlContextMenu);

        newWorkspaceMenu.setOnAction(newWorkspaceHandler);
        newTemplateMenu.setOnAction(newTemplateFileHandler);
        newModuleMenu.setOnAction(newModuleFileHandler);
        newBlockMenu.setOnAction(newBlockFileHandler);
        newXmlContextMenu.setOnAction(newContextFileHandler);

        // Open submenu
        openMenu.getItems().add(openWorkspaceMenu);

        // Add existing submenu
        MenuItem addTemplateMenu = new MenuItem("Template");
        MenuItem addModuleMenu = new MenuItem("Module");
        MenuItem addBlockMenu = new MenuItem("Block");
        MenuItem addXmlContextMenu = new MenuItem("Context XML");

        addMenu.getItems().addAll(addTemplateMenu, addModuleMenu, addBlockMenu, addXmlContextMenu);

        addTemplateMenu.setOnAction(addTemplateFileHandler);
        addModuleMenu.setOnAction(addModuleFileHandler);
        addBlockMenu.setOnAction(addBlockFileHandler);
        addXmlContextMenu.setOnAction(addContextFileHandler);

        MenuItem saveCurrent = new MenuItem("Save Current");

        saveCurrent.setAccelerator(
                new KeyCodeCombination(KeyCode.S, KeyCombination.CONTROL_DOWN)
        );
        MenuItem saveCurrentAs = new MenuItem("Save Current As");

        saveCurrent.setOnAction(saveCurrentFileHandler);
        saveCurrentAs.setOnAction(saveCurrentAsFileHandler);
        exitItem.setOnAction(exitHandler);

        fileMenu.getItems().addAll(newMenu, openMenu, addMenu, new SeparatorMenuItem(), saveCurrent, saveCurrentAs, saveWorkspaceMenu, new SeparatorMenuItem(), exitItem);

        // Edit menu
        Menu editMenu = new Menu("Edit");
        MenuItem findMenu = new MenuItem("Find");
        findMenu.setAccelerator(
                new KeyCodeCombination(KeyCode.F, KeyCombination.CONTROL_DOWN)
        );
        findMenu.setOnAction(findHandler);

        editMenu.getItems().add(findMenu);

        // View menu
        Menu viewMenu = new Menu("View");
        MenuItem darkThemeItem = new MenuItem("Dark Theme");
        darkThemeItem.setOnAction(applyDarkThemeHandler);
        viewMenu.getItems().add(darkThemeItem);

        MenuItem lightThemeItem = new MenuItem("Light Theme");
        lightThemeItem.setOnAction(applyLightThemeHandler);
        viewMenu.getItems().add(lightThemeItem);

        //Help Menu
        Menu helpMenu = new Menu("Help");
        MenuItem aboutItem = new MenuItem("About");
        aboutItem.setOnAction(helpAboutHandler);
        helpMenu.getItems().add(aboutItem);


        // --- Add menus to menu bar ---
        menuBar.getMenus().addAll(fileMenu, editMenu, viewMenu, helpMenu);

        // Set styling
        menuBar.getStyleClass().add("main-menu-bar");
    }

    @Override
    public Node getNode() {
        return menuBar;
    }
}
