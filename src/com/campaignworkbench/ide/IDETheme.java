package com.campaignworkbench.ide;

/**
 * Visual theme applicable to the entire IDE user interface
 */
public enum IDETheme {

    DARK (
            "/richtextfx/campaign_syntax_styles_dark.css",
            "/richtextfx/xml_syntax_styles_dark.css",
            "/richtextfx/html_styles_dark.css",
            "/richtextfx/codeeditor_styles_dark.css"
    ),
    LIGHT (
            "/richtextfx/campaign_styles_light.css",
            "/richtextfx/xml_styles_light.css",
            "/richtextfx/html_styles_light.css",
            "/richtextfx/codeeditor_styles_light.css"
    );

    private final String codeEditorStyleSheet;
    private final String campaignStyleSheet;
    private final String xmlStyleSheet;
    private final String htmlStyleSheet;

    IDETheme(String campaignStyleSheet, String xmlStyleSheet, String htmlStyleSheet, String codeEditorStyleSheet) {
        this.campaignStyleSheet = campaignStyleSheet;
        this.xmlStyleSheet = xmlStyleSheet;
        this.htmlStyleSheet = htmlStyleSheet;
        this.codeEditorStyleSheet = codeEditorStyleSheet;
    }

    public String getCodeEditorStyleSheet() {
        return codeEditorStyleSheet;
    }

    public String getCampaignStyleSheet() {
        return getStylesFromStyleSheet(campaignStyleSheet);
    }

    public String getXmlStyleSheet() {
        return getStylesFromStyleSheet(xmlStyleSheet);
    }

    public String getHtmlStyleSheet() {
        return getStylesFromStyleSheet(htmlStyleSheet);
    }

    private String getStylesFromStyleSheet(String styleSheet) {
        return this.getClass().getResource(styleSheet).toExternalForm();
    }
}