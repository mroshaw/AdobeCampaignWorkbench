package com.campaignworkbench.ide;

import com.campaignworkbench.ide.editor.ICodeEditor;
import com.campaignworkbench.ide.editor.RSyntaxEditor;
import com.campaignworkbench.ide.editor.SyntaxType;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.web.WebView;

/**
 * Implements an HTML preview panel for use in the IDE User Interface
 */
public class OutputTabPanel implements IJavaFxNode, IThemeable {

    private final TabPane tabPane;
    private final Tab webViewTab;
    private final OutputTab sourceCodeTab;
    private final OutputTab preSourceCodeTab;

    private final WebView webView;

    /**
     * The label for the preview panel
     */
    Label outputLabel;

    /**
     * Constructor
     * @param label The label for the preview panel
     */
    public OutputTabPanel(String label) {
        tabPane = new TabPane();

        // Web View
        webView = new WebView();
        webView.setCursor(Cursor.TEXT);

        webViewTab = new Tab("Web View", webView);
        webViewTab.setClosable(false);
        sourceCodeTab = new OutputTab("HTML Source", SyntaxType.HTML_PREVIEW);
        preSourceCodeTab = new OutputTab("JS Pre Source", SyntaxType.SOURCE_PREVIEW);

        tabPane.getTabs().addAll(webViewTab, sourceCodeTab, preSourceCodeTab);

        tabPane.getSelectionModel().selectedItemProperty().addListener((obs, oldTab, selectedTab) -> refreshTab(selectedTab));

        ThemeManager.register(this);
    }

    private void refreshTab(Tab tab) {

        if(tab instanceof OutputTab outputTab) {
            outputTab.refreshContent();
        }
    }

    /**
     * @param htmlContent HTML preview content to set in the UI
     * @param jsSourceContent JavaScript pre-processing source
     */
    public void setContent(String htmlContent, String jsSourceContent) {
        setWebContent(htmlContent);
        setSourceCodeContent(htmlContent);
        setPreSourceCodeContent(jsSourceContent);
    }

    private void setWebContent(String content) {
        webView.getEngine().loadContent(content);
    }

    private void setSourceCodeContent(String content) {
        sourceCodeTab.setContentText(content);
    }


    private void setPreSourceCodeContent(String content) {
        preSourceCodeTab.setContentText(content);
    }

    @Override
    public Node getNode() {
        return tabPane;
    }

    @Override
    public void applyTheme(IDETheme ideTheme) {
        switch(ideTheme) {
            case LIGHT:
                setBackgroundColor("#ffffff");
                break;
            case DARK:
                setBackgroundColor("#1C1C1E");
                break;
        }
    }

    private void setBackgroundColor(String color) {
        String script =
                "(function() {" +
                        "  let style = document.getElementById('javafx-bg-style');" +
                        "  if (!style) {" +
                        "    style = document.createElement('style');" +
                        "    style.id = 'javafx-bg-style';" +
                        "    document.head.appendChild(style);" +
                        "  }" +
                        "  style.innerHTML = 'html, body { background: " + color + " !important; }';" +
                        "})();";

        webView.getEngine().executeScript(script);
    }

}
