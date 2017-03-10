/*
 * Copyright (c) 2017 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.ogc.wms;

import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.util.xml.XmlModel;

public class WmsGeographicBoundingBox extends XmlModel {

    protected double north = Double.NaN;

    protected double east = Double.NaN;

    protected double south = Double.NaN;

    protected double west = Double.NaN;

    public WmsGeographicBoundingBox() {
    }

    public Sector getGeographicBoundingBox() {
        double deltaLongitude = this.east - this.west;
        double deltaLatitude = this.north - this.south;
        return new Sector(this.south, this.west, deltaLatitude, deltaLongitude);
    }

    @Override
    public void parseField(String keyName, Object value) {
        if (keyName.equals("minx")) {
            this.west = Double.parseDouble((String) value);
        } else if (keyName.equals("miny")) {
            this.south = Double.parseDouble((String) value);
        } else if (keyName.equals("maxx")) {
            this.east = Double.parseDouble((String) value);
        } else if (keyName.equals("maxy")) {
            this.north = Double.parseDouble((String) value);
        } else if (keyName.equals("westBoundLongitude")) {
            this.west = Double.parseDouble((String) value);
        } else if (keyName.equals("southBoundLatitude")) {
            this.south = Double.parseDouble((String) value);
        } else if (keyName.equals("eastBoundLongitude")) {
            this.east = Double.parseDouble((String) value);
        } else if (keyName.equals("northBoundLatitude")) {
            this.north = Double.parseDouble((String) value);
        }
    }
}
