/*
 * Copyright (c) 2017 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.ogc.wmts;

import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.util.Logger;
import gov.nasa.worldwind.util.xml.XmlModel;

public class OwsBoundingBox extends XmlModel {

    protected String crs;

    protected String lowerCorner;

    protected String upperCorner;

    public String getLowerCorner() {
        return this.lowerCorner;
    }

    public String getUpperCorner() {
        return this.upperCorner;
    }

    public String getCrs() {
        return this.crs;
    }

    public Sector getSector() {
        if (this.crs == null) {
            return null;
        } else if (this.crs.equals("urn:ogc:def:crs:OGC:1.3:CRS84") || this.crs.equals("http://www.opengis.net/def/crs/OGC/1.3/CRS84")) {
            double[] lowerLeft = this.parse2dCornerString(this.lowerCorner, true);
            if (lowerLeft == null) {
                return null;
            }
            double[] upperRight = this.parse2dCornerString(this.upperCorner, true);
            if (upperRight == null) {
                return null;
            }
            return new Sector(lowerLeft[1], lowerLeft[0], upperRight[1] - lowerLeft[1], upperRight[0] - lowerLeft[0]);
        } else if (this.crs.equals("urn:ogc:def:crs:EPSG::4326")) {
            double[] lowerLeft = this.parse2dCornerString(this.lowerCorner, false);
            if (lowerLeft == null) {
                return null;
            }
            double[] upperRight = this.parse2dCornerString(this.upperCorner, false);
            if (upperRight == null) {
                return null;
            }
            return new Sector(lowerLeft[1], lowerLeft[0], upperRight[1] - lowerLeft[1], upperRight[0] - lowerLeft[0]);
        } else {
            return null;
        }
    }

    @Override
    protected void parseField(String keyName, Object value) {
        if (keyName.equals("crs")) {
            this.crs = (String) value;
        } else if (keyName.equals("LowerCorner")) {
            this.lowerCorner = (String) value;
        } else if (keyName.equals("UpperCorner")) {
            this.upperCorner = (String) value;
        }
    }

    protected double[] parse2dCornerString(String value, boolean xIsFirstArgument) {
        String[] values = value.split("\\s+");

        if (values.length == 2) {
            double x = Double.parseDouble(values[0]);
            double y = Double.parseDouble(values[1]);
            if (xIsFirstArgument) {
                return new double[]{x, y};
            } else {
                return new double[]{y, x};
            }
        } else {
            Logger.logMessage(Logger.WARN, "OwsBoundingBox", "parseCornerString", "Error parsing value: " + value);
            return null;
        }
    }
}
