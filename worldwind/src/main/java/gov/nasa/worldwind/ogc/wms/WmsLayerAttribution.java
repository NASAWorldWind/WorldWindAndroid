/*
 * Copyright (c) 2016 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.ogc.wms;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;

import javax.xml.namespace.QName;

import gov.nasa.worldwind.util.xml.XmlModel;
import gov.nasa.worldwind.util.xml.XmlPullParserContext;

public class WmsLayerAttribution extends XmlModel {

    protected QName TITLE;

    protected QName ONLINE_RESOURCE;

    protected QName LOGO_URL;

    protected String title;

    protected WmsOnlineResource onlineResource;

    protected WmsLogoUrl logoURL;

    public WmsLayerAttribution(String namespaceURI) {
        super(namespaceURI);

        this.initialize();
    }

    private void initialize() {
        TITLE = new QName(this.getNamespaceUri(), "Title");
        ONLINE_RESOURCE = new QName(this.getNamespaceUri(), "OnlineResource");
        LOGO_URL = new QName(this.getNamespaceUri(), "LogoURL");
    }

    // TODO remove once thoroughly tested
    // This is the first unique use of allocate observed. The object type return should now be captured by the
    // XmlPullParserContext registry. Retaining in case the behavior isn't as expected.
//    @Override
//    public XmlEventParser allocate(XmlEventParserContext ctx, XmlEvent event)
//    {
//        XmlEventParser defaultParser = null;
//
//        if (ctx.isStartElement(event, ONLINE_RESOURCE))
//            defaultParser = new OGCOnlineResource(this.getNamespaceURI());
//        else if (ctx.isStartElement(event, LOGO_URL))
//            defaultParser = new WmsLayerInfoURL(this.getNamespaceURI());
//
//        return ctx.allocate(event, defaultParser);
//    }

    @Override
    protected void doParseEventContent(XmlPullParserContext ctx) throws XmlPullParserException, IOException {

        XmlPullParser xpp = ctx.getParser();

        if (ctx.isStartElement(this.TITLE)) {
            this.setTitle(xpp.getName().trim());
        } else if (ctx.isStartElement(this.ONLINE_RESOURCE)) {
            XmlModel model = ctx.createParsableModel(this.ONLINE_RESOURCE);
            if (model != null) {
                Object o = model.read(ctx);
                if (o != null) {
                    this.setOnlineResource((WmsOnlineResource) o);
                }
            }
        } else if (ctx.isStartElement(this.LOGO_URL)) {
            XmlModel model = ctx.createParsableModel(this.LOGO_URL);
            if (model != null) {
                Object o = model.read(ctx);
                if (o != null) {
                    this.setLogoURL((WmsLogoUrl) o);
                }
            }
        }
    }

    public String getTitle() {
        return title;
    }

    protected void setTitle(String title) {
        this.title = title;
    }

    public WmsOnlineResource getOnlineResource() {
        return onlineResource;
    }

    protected void setOnlineResource(WmsOnlineResource onlineResource) {
        this.onlineResource = onlineResource;
    }

    public WmsLogoUrl getLogoURL() {
        return logoURL;
    }

    protected void setLogoURL(WmsLogoUrl logoURL) {
        this.logoURL = logoURL;
    }
}
