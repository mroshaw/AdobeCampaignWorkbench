package com.campaignworkbench.ide;

import atlantafx.base.theme.CupertinoDark;
import atlantafx.base.theme.CupertinoLight;

import java.net.URL;

/**
 * Visual theme applicable to the entire IDE user interface
 */
public enum IDETheme {

    DARK (
            new CupertinoDark().getUserAgentStylesheet(),
            "/styles/richtextfx/ide_styles_dark.css",
            "/styles/richtextfx/campaign_syntax_styles_dark.css",
            "/styles/richtextfx/xml_syntax_styles_dark.css",
            "/styles/richtextfx/html_syntax_styles_dark.css"
    ),
    LIGHT (
            new CupertinoLight().getUserAgentStylesheet(),
            "/styles/richtextfx/ide_styles_light.css",
            "/styles/richtextfx/campaign_syntax_styles_light.css",
            "/styles/richtextfx/xml_syntax_styles_light.css",
            "/styles/richtextfx/html_syntax_styles_light.css"
    );

    private final String atlantaFxStyleSheet;
    private final String ideStyleSheet;
    private final String campaignSyntaxStyleSheet;
    private final String xmlSyntaxStyleSheet;
    private final String htmlSyntaxStyleSheet;

    IDETheme(String atlantaFxStyleSheet, String ideStyleSheet, String campaignSyntaxStyleSheet, String xmlSyntaxStyleSheet, String htmlSyntaxStyleSheet) {
        this.atlantaFxStyleSheet = atlantaFxStyleSheet;
        this.campaignSyntaxStyleSheet = campaignSyntaxStyleSheet;
        this.xmlSyntaxStyleSheet = xmlSyntaxStyleSheet;
        this.htmlSyntaxStyleSheet = htmlSyntaxStyleSheet;
        this.ideStyleSheet = ideStyleSheet;
    }

    public String getIdeStyleSheet() {
        return ideStyleSheet;
    }

    public String getCampaignSyntaxStyleSheet() {
        return getStylesFromStyleSheet(campaignSyntaxStyleSheet);
    }

    public String getXmlSyntaxStyleSheet() {
        return getStylesFromStyleSheet(xmlSyntaxStyleSheet);
    }

    public String getHtmlSyntaxStyleSheet() {
        return getStylesFromStyleSheet(htmlSyntaxStyleSheet);
    }

    public String getAtlantaFxStyleSheet() {
        return atlantaFxStyleSheet;
    }

    private String getStylesFromStyleSheet(String styleSheet) {
        URL styleSheetUrl = this.getClass().getResource(styleSheet);
        if( styleSheetUrl != null ) {
            return styleSheetUrl.toExternalForm();
        }
        else {
            throw new IDEException("Unable to locate style sheet: " + styleSheet, null);
        }
    }
}