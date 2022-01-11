/*
 * Copyright (c) 2017 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.ogc.wms;

import gov.nasa.worldwind.util.xml.XmlModel;

public class WmsBoundingBox extends XmlModel {

    protected String crs;

    protected double minx;

    protected double maxx;

    protected double miny;

    protected double maxy;

    protected double resx;

    protected double resy;

    public WmsBoundingBox() {
    }

    public String getCRS() {
        return crs;
    }

    public double getMinx() {
        return minx;
    }

    public double getMaxx() {
        return maxx;
    }

    public double getMiny() {
        return miny;
    }

    public double getMaxy() {
        return maxy;
    }

    public double getResx() {
        return resx;
    }

    public double getResy() {
        return resy;
    }

    @Override
    public void parseField(String keyName, Object value) {
        switch (keyName) {
            case "CRS":
            case "SRS":
                // Convention is to be in upper case
                this.crs = ((String) value).toUpperCase();
                break;
            case "minx":
                this.minx = Double.parseDouble((String) value);
                break;
            case "miny":
                this.miny = Double.parseDouble((String) value);
                break;
            case "maxx":
                this.maxx = Double.parseDouble((String) value);
                break;
            case "maxy":
                this.maxy = Double.parseDouble((String) value);
                break;
            case "resx":
                this.resx = Double.parseDouble((String) value);
                break;
            case "resy":
                this.resy = Double.parseDouble((String) value);
                break;
        }
    }
}
