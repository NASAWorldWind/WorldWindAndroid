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
        if (keyName.equals("CRS")) {
            // Convention is to be in upper case
            this.crs = ((String) value).toUpperCase();
        } else if (keyName.equals("SRS")) {
            this.crs = ((String) value).toUpperCase();
        } else if (keyName.equals("minx")) {
            this.minx = Double.parseDouble((String) value);
        } else if (keyName.equals("miny")) {
            this.miny = Double.parseDouble((String) value);
        } else if (keyName.equals("maxx")) {
            this.maxx = Double.parseDouble((String) value);
        } else if (keyName.equals("maxy")) {
            this.maxy = Double.parseDouble((String) value);
        } else if (keyName.equals("resx")) {
            this.resx = Double.parseDouble((String) value);
        } else if (keyName.equals("resy")) {
            this.resy = Double.parseDouble((String) value);
        }
    }
}
