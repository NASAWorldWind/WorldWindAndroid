/*
 * Copyright (c) 2016 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.ogc.wms;

import org.xmlpull.v1.XmlPullParser;

import gov.nasa.worldwind.util.xml.XmlPullParserContext;

public class WmsAuthorityUrl extends WmsLayerInfoUrl {

    protected String authority;

    public WmsAuthorityUrl(String namespaceURI) {
        super(namespaceURI);
    }

    @Override
    protected void doParseEventAttributes(XmlPullParserContext ctx) {

        XmlPullParser xpp = ctx.getParser();

        String authorityValue = xpp.getAttributeValue(this.getNamespaceUri(), "authority");

        if (authorityValue != null && !authorityValue.isEmpty()) {
            this.setAuthority(authorityValue.trim());
        }
    }

    public String getAuthority() {
        return authority;
    }

    protected void setAuthority(String authority) {
        this.authority = authority;
    }
}
