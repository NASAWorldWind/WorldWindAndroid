/*
 * Copyright (c) 2016 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.ogc.wms;

import javax.xml.namespace.QName;

import gov.nasa.worldwind.util.xml.DoubleModel;
import gov.nasa.worldwind.util.xml.XmlModel;

public class WmsGeographicBoundingBox extends XmlModel {

    protected QName west;

    protected QName east;

    protected QName north;

    protected QName south;

    public WmsGeographicBoundingBox(String namespaceUri) {
        super(namespaceUri);
    }

    protected void initialize() {
        this.west = new QName(this.getNamespaceUri(), "westBoundLongitude");
        this.east = new QName(this.getNamespaceUri(), "eastBoundLongitude");
        this.north = new QName(this.getNamespaceUri(), "northBoundLatitude");
        this.south = new QName(this.getNamespaceUri(), "southBoundLatitude");
    }

    protected Double getValue(QName name) {
        DoubleModel value = (DoubleModel) this.getField(name);
        if (value != null) {
            return value.getValue();
        } else {
            return null;
        }
    }

    public Double getWestBound() {
        return this.getValue(this.west);
    }

    public Double getEastBound() {
        return this.getValue(this.west);
    }

    public Double getNorthBound() {
        return this.getValue(this.west);
    }

    public Double getSouthBound() {
        return this.getValue(this.west);
    }
}
