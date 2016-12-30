/*
 * Copyright (c) 2016 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.ogc.wms;

import java.util.HashSet;
import java.util.Set;

import javax.xml.namespace.QName;

import gov.nasa.worldwind.util.xml.XmlModel;

public class WmsLayerStyle extends XmlModel {

    protected QName name;

    protected QName title;

    protected QName description;

    protected QName legendUrl;

    protected QName styleSheetUrl;

    protected QName styleUrl;

//    protected String name;
//    protected String title;
//    protected String styleAbstract;
//    protected WmsLayerInfoUrl styleSheetURL;
//    protected WmsLayerInfoUrl styleURL;
//    protected Set<WmsLogoUrl> legendURLs;

    public WmsLayerStyle(String namespaceURI) {
        super(namespaceURI);

        this.initialize();
    }

    private void initialize() {
        this.name = new QName(this.getNamespaceUri(), "Name");
        this.title = new QName(this.getNamespaceUri(), "Title");
        this.description = new QName(this.getNamespaceUri(), "Abstract");
        this.legendUrl = new QName(this.getNamespaceUri(), "LegendURL");
        this.styleSheetUrl = new QName(this.getNamespaceUri(), "StyleSheetURL");
        this.styleUrl = new QName(this.getNamespaceUri(), "StyleURL");
    }

//    @Override
//    public XmlEventParser allocate(XmlEventParserContext ctx, XmlEvent event)
//    {
//        XmlEventParser defaultParser = null;
//
//        XmlEventParser parser = super.allocate(ctx, event);
//        if (parser != null)
//            return parser;
//
//        if (ctx.isStartElement(event, LEGEND_URL))
//            defaultParser = new WmsLogoURL(this.getNamespaceURI());
//        else if (ctx.isStartElement(event, STYLE_SHEET_URL))
//            defaultParser = new WmsLayerInfoURL(this.getNamespaceURI());
//        else if (ctx.isStartElement(event, STYLE_URL))
//            defaultParser = new WmsLayerInfoURL(this.getNamespaceURI());
//
//        return ctx.allocate(event, defaultParser);
//    }

//    @Override
//    protected void doParseEventContent(XmlEventParserContext ctx, XmlEvent event, Object... args)
//        throws XMLStreamException
//    {
//        if (ctx.isStartElement(event, TITLE))
//        {
//            String s = ctx.getStringParser().parseString(ctx, event);
//            if (!WWUtil.isEmpty(s))
//                this.setTitle(s);
//        }
//        else if (ctx.isStartElement(event, NAME))
//        {
//            String s = ctx.getStringParser().parseString(ctx, event);
//            if (!WWUtil.isEmpty(s))
//                this.setName(s);
//        }
//        else if (ctx.isStartElement(event, ABSTRACT))
//        {
//            String s = ctx.getStringParser().parseString(ctx, event);
//            if (!WWUtil.isEmpty(s))
//                this.setStyleAbstract(s);
//        }
//        else if (ctx.isStartElement(event, LEGEND_URL))
//        {
//            XmlEventParser parser = this.allocate(ctx, event);
//            if (parser != null)
//            {
//                Object o = parser.parse(ctx, event, args);
//                if (o != null && o instanceof WmsLogoURL)
//                    this.addLegendURL((WmsLogoURL) o);
//            }
//        }
//        else if (ctx.isStartElement(event, STYLE_SHEET_URL))
//        {
//            XmlEventParser parser = this.allocate(ctx, event);
//            if (parser != null)
//            {
//                Object o = parser.parse(ctx, event, args);
//                if (o != null && o instanceof WmsLayerInfoURL)
//                    this.setStyleSheetURL((WmsLayerInfoURL) o);
//            }
//        }
//        else if (ctx.isStartElement(event, STYLE_URL))
//        {
//            XmlEventParser parser = this.allocate(ctx, event);
//            if (parser != null)
//            {
//                Object o = parser.parse(ctx, event, args);
//                if (o != null && o instanceof WmsLayerInfoURL)
//                    this.setStyleURL((WmsLayerInfoURL) o);
//            }
//        }
//    }

    public String getName() {
        return this.getChildCharacterValue(this.name);
    }

    protected void setName(String name) {
        this.setChildCharacterValue(this.name, name);
    }

    public String getTitle() {
        return this.getChildCharacterValue(this.title);
    }

    protected void setTitle(String title) {
        this.setChildCharacterValue(this.title, title);
    }

    public String getStyleAbstract() {
        return this.getChildCharacterValue(this.description);
    }

    protected void setStyleAbstract(String styleAbstract) {
        this.setChildCharacterValue(this.description, styleAbstract);
    }

    public WmsLayerInfoUrl getStyleSheetUrl() {
        return (WmsLayerInfoUrl) this.getField(this.styleSheetUrl);
    }

    protected void setStyleSheetUrl(WmsLayerInfoUrl styleSheetUrl) {
        this.setField(this.styleSheetUrl, styleSheetUrl);
    }

    public WmsLayerInfoUrl getStyleUrl() {
        return (WmsLayerInfoUrl) this.getField(this.styleUrl);
    }

    protected void setStyleUrl(WmsLayerInfoUrl styleUrl) {
        this.setField(this.styleUrl, styleUrl);
    }

    public Set<WmsLogoUrl> getLegendUrls() {
        return (Set<WmsLogoUrl>) this.getField(this.legendUrl);
    }

    /**
     * Sets the WmsLogoUrls associated with this layer style. This method will replace any existing legend urls.
     *
     * @param legendUrls
     */
    protected void setLegendUrls(Set<WmsLogoUrl> legendUrls) {
        Set<WmsLogoUrl> logoUrls = (Set<WmsLogoUrl>) this.getField(this.legendUrl);
        if (logoUrls != null) {
            logoUrls.clear();
            logoUrls.addAll(legendUrls);
        } else {
            logoUrls = new HashSet<>();
            logoUrls.addAll(legendUrls);
            super.setField(this.legendUrl, logoUrls);
        }
    }

    protected void addLegendUrl(WmsLogoUrl url) {
        this.setField(this.legendUrl, url);
    }

    @Override
    public void setField(QName keyName, Object value) {

        // Since we have a list, need to check for multiple values of the WmsLogoUrl
        if (keyName.getLocalPart().equals(this.legendUrl.getLocalPart())
            && keyName.getNamespaceURI().equals(this.legendUrl.getNamespaceURI())) {
            Set<WmsLogoUrl> logoUrls = (Set<WmsLogoUrl>) this.getField(this.legendUrl);
            if (logoUrls == null) {
                super.setField(this.legendUrl, new HashSet<WmsLogoUrl>());
                logoUrls = (Set<WmsLogoUrl>) super.getField(this.legendUrl);
            }

            if (value instanceof WmsLogoUrl) {
                logoUrls.add((WmsLogoUrl) value);
                return;
            }
        }

        super.setField(keyName, value);
    }
}
