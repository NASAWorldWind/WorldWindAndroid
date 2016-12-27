/*
 * Copyright (c) 2016 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.ogc.wms;

import org.xmlpull.v1.XmlPullParser;

import javax.xml.namespace.QName;

import gov.nasa.worldwind.util.xml.XmlModel;
import gov.nasa.worldwind.util.xml.XmlPullParserContext;

public class WmsOnlineResource extends XmlModel
{

    /**
     * The default namespace for describing an xlink.
     */
    public static final String DEFAULT_NAMESPACE = "http://www.w3.org/1999/xlink";

    protected QName HREF;
    protected QName TYPE;

    protected String type;
    protected String href;

    public WmsOnlineResource() {
        super(XmlPullParserContext.DEFAULT_NAMESPACE);
    }

    public WmsOnlineResource(String namespaceURI)
    {
        super(namespaceURI == null ? XmlPullParserContext.DEFAULT_NAMESPACE : namespaceURI);

        this.initialize();
    }

    private void initialize()
    {
        HREF = new QName(DEFAULT_NAMESPACE, "href");
        TYPE = new QName(DEFAULT_NAMESPACE, "type");
    }

    @Override
    protected void doParseEventAttributes(XmlPullParserContext ctx)
    {
        XmlPullParser xpp = ctx.getParser();

        this.setType(xpp.getAttributeValue(TYPE.getNamespaceURI(), "type"));
        this.setHref(xpp.getAttributeValue(HREF.getNamespaceURI(), "href"));
    }

    public String getType()
    {
        return type;
    }

    protected void setType(String type)
    {
        this.type = type;
    }

    public String getHref()
    {
        return href;
    }

    protected void setHref(String href)
    {
        this.href = href;
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();

        sb.append("href: ").append(this.href != null ? this.href : "null");
        sb.append(", type: ").append(this.type != null ? this.type : "null");

        return sb.toString();
    }
}
