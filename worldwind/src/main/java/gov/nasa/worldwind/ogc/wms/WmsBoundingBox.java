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

public class WmsBoundingBox extends XmlModel {

    protected static String BOUNDING_BOX_ATTRIBUTE_NS = "";

    protected static QName CRS = new QName(BOUNDING_BOX_ATTRIBUTE_NS, "CRS");

    protected static QName SRS = new QName(BOUNDING_BOX_ATTRIBUTE_NS, "SRS");

    protected static QName MINX = new QName(BOUNDING_BOX_ATTRIBUTE_NS, "minx");

    protected static QName MINY = new QName(BOUNDING_BOX_ATTRIBUTE_NS, "miny");

    protected static QName MAXX = new QName(BOUNDING_BOX_ATTRIBUTE_NS, "maxx");

    protected static QName MAXY = new QName(BOUNDING_BOX_ATTRIBUTE_NS, "maxy");

    protected static QName RESX = new QName(BOUNDING_BOX_ATTRIBUTE_NS, "resx");

    protected static QName RESY = new QName(BOUNDING_BOX_ATTRIBUTE_NS, "resy");

    protected String crs;

    protected double minx;

    protected double maxx;

    protected double miny;

    protected double maxy;

    protected double resx;

    protected double resy;

    public static WmsBoundingBox createFromStrings(String crs, String minx, String maxx, String miny, String maxy,
                                                   String resx, String resy) {
        WmsBoundingBox bbox = new WmsBoundingBox(null);

        try {
            bbox.crs = crs;
            bbox.minx = Double.parseDouble(minx);
            bbox.maxx = Double.parseDouble(maxx);
            bbox.miny = Double.parseDouble(miny);
            bbox.maxy = Double.parseDouble(maxy);
            bbox.resx = resx != null && !resx.equals("") ? Double.parseDouble(resx) : 0;
            bbox.resy = resy != null && !resy.equals("") ? Double.parseDouble(resy) : 0;
        } catch (NumberFormatException e) {
            // TODO log error and handle
        }

        return bbox;
    }

    public WmsBoundingBox(String namespaceURI) {
        super(namespaceURI);
    }

    @Override
    protected void doParseEventAttributes(XmlPullParserContext ctx) throws XmlPullParserException, IOException {

        XmlPullParser xpp = ctx.getParser();

        String referenceSystem = xpp.getAttributeValue(CRS.getNamespaceURI(), CRS.getLocalPart());
        if (referenceSystem == null) {
            referenceSystem = xpp.getAttributeValue(SRS.getNamespaceURI(), SRS.getLocalPart());
        }

        if (referenceSystem != null && !referenceSystem.isEmpty()) {
            this.setCRS(referenceSystem);
        }

        String attrValue = xpp.getAttributeValue(MINX.getNamespaceURI(), MINX.getLocalPart());
        Double value = this.parseDouble(attrValue);
        if (value != null) {
            this.setMinx(value);
        }

        attrValue = xpp.getAttributeValue(MINY.getNamespaceURI(), MINY.getLocalPart());
        value = this.parseDouble(attrValue);
        if (value != null) {
            this.setMiny(value);
        }

        attrValue = xpp.getAttributeValue(MAXX.getNamespaceURI(), MAXX.getLocalPart());
        value = this.parseDouble(attrValue);
        if (value != null) {
            this.setMaxx(value);
        }

        attrValue = xpp.getAttributeValue(MAXY.getNamespaceURI(), MAXY.getLocalPart());
        value = this.parseDouble(attrValue);
        if (value != null) {
            this.setMaxy(value);
        }

        attrValue = xpp.getAttributeValue(RESX.getNamespaceURI(), RESX.getLocalPart());
        value = this.parseDouble(attrValue);
        if (value != null) {
            this.setResx(value);
        }

        attrValue = xpp.getAttributeValue(RESY.getNamespaceURI(), RESY.getLocalPart());
        value = this.parseDouble(attrValue);
        if (value != null) {
            this.setResy(value);
        }
    }

    public String getCRS() {
        return crs;
    }

    protected void setCRS(String crs) {
        this.crs = crs;
    }

    public double getMinx() {
        return minx;
    }

    protected void setMinx(double minx) {
        this.minx = minx;
    }

    public double getMaxx() {
        return maxx;
    }

    protected void setMaxx(double maxx) {
        this.maxx = maxx;
    }

    public double getMiny() {
        return miny;
    }

    protected void setMiny(double miny) {
        this.miny = miny;
    }

    public double getMaxy() {
        return maxy;
    }

    protected void setMaxy(double maxy) {
        this.maxy = maxy;
    }

    public double getResx() {
        return resx;
    }

    protected void setResx(double resx) {
        this.resx = resx;
    }

    public double getResy() {
        return resy;
    }

    protected void setResy(double resy) {
        this.resy = resy;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        sb.append(this.crs);
        sb.append(": minx = ");
        sb.append(this.minx);
        sb.append(" miny = ");
        sb.append(this.miny);
        sb.append(" maxx = ");
        sb.append(this.maxx);
        sb.append(" maxy = ");
        sb.append(this.maxy);
        sb.append(" resx = ");
        sb.append(this.resx);
        sb.append(" resy = ");
        sb.append(this.resy);

        return sb.toString();
    }

    protected Double parseDouble(String value) {

        Double parsedValue = null;

        if (value != null && !value.isEmpty()) {
            try {
                parsedValue = Double.parseDouble(value);
            } catch (NumberFormatException e) {
                // TODO log and handle
            }
        }

        return parsedValue;
    }
}
