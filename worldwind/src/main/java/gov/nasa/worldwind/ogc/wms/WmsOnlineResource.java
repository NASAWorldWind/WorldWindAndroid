/*
 * Copyright (c) 2016 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.ogc.wms;

import javax.xml.namespace.QName;

import gov.nasa.worldwind.util.xml.XmlModel;
import gov.nasa.worldwind.util.xml.XmlPullParserContext;

public class WmsOnlineResource extends XmlModel
{

    /**
     * The default namespace for describing an xlink.
     */
    public static final String DEFAULT_NAMESPACE = "http://www.w3.org/1999/xlink";

    protected static QName HREF = new QName(DEFAULT_NAMESPACE, "href");

    protected static QName TYPE = new QName(DEFAULT_NAMESPACE, "type");

    public WmsOnlineResource() {
        super(XmlPullParserContext.DEFAULT_NAMESPACE);
    }

    public WmsOnlineResource(String namespaceURI)
    {
        super(namespaceURI == null ? XmlPullParserContext.DEFAULT_NAMESPACE : namespaceURI);
    }

//    @Override
//    protected void doParseEventAttributes(XmlPullParserContext ctx)
//    {
//        XmlPullParser xpp = ctx.getParser();
//
//        this.setType(xpp.getAttributeValue(this.type.getNamespaceURI(), "type"));
//        this.setHref(xpp.getAttributeValue(this.href.getNamespaceURI(), "href"));
//    }

    public String getType()
    {
        return this.getField(TYPE).toString();
    }

    protected void setType(String type)
    {
        this.setField(TYPE, type);
    }

    public String getHref()
    {
        return this.getField(HREF).toString();
    }

    protected void setHref(String href)
    {
        this.setField(HREF, href);
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();

        sb.append("href: ").append(this.getHref() != null ? this.getHref() : "null");
        sb.append(", type: ").append(this.getType() != null ? this.getType() : "null");

        return sb.toString();
    }
}
