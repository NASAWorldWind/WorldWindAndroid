/*
 * Copyright (c) 2017 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.ogc.wmts;

import gov.nasa.worldwind.util.xml.XmlModel;

public class OwsBoundingBox extends XmlModel {

    protected String crs;

    protected Double minx;

    protected Double maxx;

    protected Double miny;

    protected Double maxy;

    public String getCrs() {
        return this.crs;
    }

    public Double getMinX() {
        return this.minx;
    }

    public Double getMaxX() {
        return this.maxx;
    }

    public Double getMinY() {
        return this.miny;
    }

    public Double getMaxY() {
        return this.maxy;
    }

    @Override
    protected void parseField(String keyName, Object value) {
        if (keyName.equals("crs")) {
            this.crs = (String) value;
        } else if (keyName.equals("LowerCorner")) {
            this.parseCornerString((String) value);
        } else if (keyName.equals("UpperCorner")) {
            this.parseCornerString((String) value);
        }
    }

    protected void parseCornerString(String value) {
        String[] values = value.split("\\s+");

        if (values.length == 2) {
            double x = Double.parseDouble(values[0]);
            double y = Double.parseDouble(values[1]);
            minx = (minx != null) ? Math.min(x, minx) : x;
            miny = (miny != null) ? Math.min(y, miny) : y;
            maxx = (maxx != null) ? Math.max(x, maxx) : x;
            maxy = (maxy != null) ? Math.max(y, maxy) : y;
        } else {
            // TODO log message
        }
    }
}
