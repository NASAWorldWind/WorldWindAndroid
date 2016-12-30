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

public class WmsLayerInfoUrl extends XmlModel {

    protected QName FORMAT;

    protected QName ONLINE_RESOURCE;

    protected WmsOnlineResource onlineResource;

    protected String name;

    protected String format;

    public WmsLayerInfoUrl(String namespaceUri) {
        super(namespaceUri);

        this.initialize();
    }

    private void initialize() {
        FORMAT = new QName(this.getNamespaceUri(), "Format");
        ONLINE_RESOURCE = new QName(this.getNamespaceUri(), "OnlineResource");
    }

    @Override
    protected void doParseEventContent(XmlPullParserContext ctx)
        throws XmlPullParserException, IOException {

        XmlPullParser xpp = ctx.getParser();

        if (xpp.getEventType() == XmlPullParser.START_TAG) {

            QName event = new QName(xpp.getNamespace(), xpp.getName());

            if (event.equals(FORMAT)) {
                if (xpp.next() == XmlPullParser.TEXT) {
                    this.setFormat(xpp.getText().trim());
                }
            } else if (event.equals(ONLINE_RESOURCE)) {
                XmlModel elementModel = ctx.createParsableModel(ONLINE_RESOURCE);
                Object o = elementModel.read(ctx);
                if (o != null && o instanceof WmsOnlineResource) {
                    this.setOnlineResource((WmsOnlineResource) o);
                }
            }
        }
    }

    @SuppressWarnings({"UnusedDeclaration"})
    protected void doParseEventAttributes(XmlPullParserContext ctx) {

        XmlPullParser xpp = ctx.getParser();

        String nameValue = xpp.getAttributeValue(this.getNamespaceUri(), "name");

        if (nameValue != null && !nameValue.isEmpty()) {
            this.setName(nameValue);
        }
    }

    public WmsOnlineResource getOnlineResource() {
        return onlineResource;
    }

    protected void setOnlineResource(WmsOnlineResource onlineResource) {
        this.onlineResource = onlineResource;
    }

    public String getName() {
        return name;
    }

    protected void setName(String name) {
        this.name = name;
    }

    public String getFormat() {
        return format;
    }

    protected void setFormat(String format) {
        this.format = format;
    }
}
