/*
 * Copyright (c) 2017 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.ogc.wmts;

import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.util.Logger;
import gov.nasa.worldwind.util.xml.XmlModel;

/**
 * An implementation of the OGC Web Services Common Bounding Box. The EPSG:4326 coordinate system axis ordering is
 * explicitly handled, for all other coordinate systems the configuration assumes coordinate order to be x then y for
 * the corner values. See the {@link OwsBoundingBox#parseCornerString(String)} for more information and implementation
 * details.
 */
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

    public Sector getSector() {
        return Sector.fromDegrees(this.miny, this.minx, (this.maxy - this.miny), (this.maxx - this.minx));
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

        // Correct for coordinate ordering
        // CRS84 orders coordinates in lon, lat (x, y), EPSG:4326 orders coordinates lat, lon (y, x)
        if (this.crs != null && this.crs.equals("EPSG:4326")) {
            String lat = values[0];
            values[0] = values[1];
            values[1] = lat;
        }

        if (values.length == 2) {
            double x = Double.parseDouble(values[0]);
            double y = Double.parseDouble(values[1]);
            minx = (minx != null) ? Math.min(x, minx) : x;
            miny = (miny != null) ? Math.min(y, miny) : y;
            maxx = (maxx != null) ? Math.max(x, maxx) : x;
            maxy = (maxy != null) ? Math.max(y, maxy) : y;
        } else {
            Logger.logMessage(Logger.WARN, "OwsBoundingBox", "parseCornerString", "Error parsing value: " + value);
        }
    }
}
