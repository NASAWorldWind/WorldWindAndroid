/*
 * Copyright (c) 2016 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.ogc.wms;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;

import gov.nasa.worldwind.util.xml.XmlModel;
import gov.nasa.worldwind.util.xml.XmlPullParserContext;

public class WmsLogoUrl extends XmlModel {

    protected Integer width;

    protected Integer height;

    public WmsLogoUrl(String namespaceURI) {
        super(namespaceURI);
    }

    @Override
    protected void doParseEventAttributes(XmlPullParserContext ctx) throws XmlPullParserException, IOException {

        super.doParseEventAttributes(ctx);

        XmlPullParser xpp = ctx.getParser();

        try {
            int width = Integer.parseInt(xpp.getAttributeValue("", "width"));
            this.setWidth(width);
        } catch (NumberFormatException e) {
            // TODO log the exception
        }

        try {
            int height = Integer.parseInt(xpp.getAttributeValue("", "height"));
            this.setHeight(height);
        } catch (NumberFormatException e) {
            // TODO log the exception
        }
    }

    public Integer getWidth() {
        return width;
    }

    protected void setWidth(Integer width) {
        this.width = width;
    }

    public Integer getHeight() {
        return height;
    }

    protected void setHeight(Integer height) {
        this.height = height;
    }
}
