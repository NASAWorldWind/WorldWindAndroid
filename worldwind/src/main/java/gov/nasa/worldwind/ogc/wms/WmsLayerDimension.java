/*
 * Copyright (c) 2016 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.ogc.wms;

import javax.xml.namespace.QName;

import gov.nasa.worldwind.util.xml.XmlModel;

public class WmsLayerDimension extends XmlModel {

    protected QName name;

    protected QName units;

    protected QName unitSymbol;

    protected QName defaultValue;

    protected QName multipleValues;

    protected QName nearestValue;

    protected QName current;

    public WmsLayerDimension(String defaultNamespaceUri) {
        super(defaultNamespaceUri);
    }

    protected void initialize() {
        this.name = new QName("", "name");
        this.units = new QName("", "units");
        this.unitSymbol = new QName("", "unitSymbol");
        this.defaultValue = new QName("", "default");
        this.multipleValues = new QName("", "multipleValues");
        this.nearestValue = new QName("", "nearestValue");
        this.current = new QName("", "current");
    }

    public String getName() {
        Object o = this.getField(this.name);
        return o != null ? o.toString() : null;
    }

    public String getUnits() {
        Object o = this.getField(this.units);
        return o != null ? o.toString() : null;
    }

    public String getUnitSymbol() {
        Object o = this.getField(this.unitSymbol);
        return o != null ? o.toString() : null;
    }

    public String getDefaultValue() {
        Object o = this.getField(this.defaultValue);
        return o != null ? o.toString() : null;
    }

    public Boolean getMultipleValues() {
        return this.getBooleanAttributeValue(this.multipleValues, false);
    }

    public Boolean getNearestValue() {
        return this.getBooleanAttributeValue(this.nearestValue, false);
    }

    public Boolean getCurrent() {
        return this.getBooleanAttributeValue(this.current, false);
    }
}
