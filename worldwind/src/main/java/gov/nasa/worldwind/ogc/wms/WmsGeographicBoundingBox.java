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

    protected QName minx;

    protected QName miny;

    protected QName maxx;

    protected QName maxy;

    public WmsGeographicBoundingBox(String namespaceUri) {
        super(namespaceUri);
        this.initialize();
    }

    protected void initialize() {
        this.west = new QName(this.getNamespaceUri(), "westBoundLongitude");
        this.east = new QName(this.getNamespaceUri(), "eastBoundLongitude");
        this.north = new QName(this.getNamespaceUri(), "northBoundLatitude");
        this.south = new QName(this.getNamespaceUri(), "southBoundLatitude");
        this.minx = new QName("", "minx");
        this.miny = new QName("", "miny");
        this.maxx = new QName("", "maxx");
        this.maxy = new QName("", "maxy");
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

        // Default to handling the 1.3.0 style
        Double value = this.getParsedDoubleElementValue(this.west);
        if (value == null) {
            // try the 1.1.1 style
            value = this.getParsedDoubleAttributeValue(this.minx);
        }

        return value;
    }

    public Double getEastBound() {
        Double value = this.getParsedDoubleElementValue(this.east);
        if (value == null) {
            value = this.getParsedDoubleAttributeValue(this.maxx);
        }

        return value;
    }

    public Double getNorthBound() {
        Double value = this.getParsedDoubleElementValue(this.north);
        if (value == null) {
            value = this.getParsedDoubleAttributeValue(this.maxy);
        }

        return value;
    }

    public Double getSouthBound() {
        Double value = this.getParsedDoubleElementValue(this.south);
        if (value == null) {
            value = this.getParsedDoubleAttributeValue(this.miny);
        }

        return value;
    }

    protected Double getParsedDoubleElementValue(QName name) {
        String textValue = this.getChildCharacterValue(name);
        if (textValue != null && !textValue.isEmpty()) {
            try {
                return Double.parseDouble(textValue);
            } catch (NumberFormatException ignore) {

            }
        }

        return null;
    }

    protected Double getParsedDoubleAttributeValue(QName name) {
        Object o = this.getField(name);
        if (o != null) {
            try {
                return Double.parseDouble(o.toString());
            } catch (NumberFormatException ignore) {

            }
        }
        return null;
    }
}
