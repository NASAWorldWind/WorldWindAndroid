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
        this.initialize();
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
        return this.getParsedDoubleValue(this.west);
    }

    public Double getEastBound() {
        return this.getParsedDoubleValue(this.east);
    }

    public Double getNorthBound() {
        return this.getParsedDoubleValue(this.north);
    }

    public Double getSouthBound() {
        return this.getParsedDoubleValue(this.south);
    }

    protected Double getParsedDoubleValue(QName name) {
        String textValue = this.getChildCharacterValue(name);
        if (textValue != null && !textValue.isEmpty()) {
            try {
                return Double.parseDouble(textValue);
            } catch (NumberFormatException ignore) {

            }
        }

        return null;
    }
}
