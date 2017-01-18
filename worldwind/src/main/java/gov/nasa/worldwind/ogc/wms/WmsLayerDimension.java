/*
 * Copyright (c) 2017 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.ogc.wms;

import gov.nasa.worldwind.util.Logger;
import gov.nasa.worldwind.util.xml.XmlModel;

public class WmsLayerDimension extends XmlModel {

    protected String name;

    protected String units;

    protected String unitSymbol;

    protected String defaultValue;

    protected Boolean multipleValues;

    protected Boolean nearestValue;

    protected Boolean current;

    public WmsLayerDimension() {}

    public String getName() {
        return this.name;
    }

    public String getUnits() {
        return this.units;
    }

    public String getUnitSymbol() {
        return this.unitSymbol;
    }

    public String getDefaultValue() {
        return this.defaultValue;
    }

    public Boolean getMultipleValues() {
        return this.multipleValues;
    }

    public Boolean getNearestValue() {
        return this.nearestValue;
    }

    public Boolean getCurrent() {
        return this.current;
    }

    protected Boolean parseBoolean(Object value) {
        try {
            return Boolean.parseBoolean(value.toString());
        } catch (Exception e) {
            Logger.makeMessage("WmsLayerCapabilities", "parseDouble", e.toString());
        }
        return null;
    }

    @Override
    public void setField(String keyName, Object value) {
        if (keyName.equals("name")) {
            this.name = value.toString();
        } else if (keyName.equals("units")) {
            this.units = value.toString();
        } else if (keyName.equals("unitSymbol")) {
            this.unitSymbol = value.toString();
        } else if (keyName.equals("default")) {
            this.defaultValue = value.toString();
        } else if (keyName.equals("multipleValues")) {
            this.multipleValues = this.parseBoolean(value);
        } else if (keyName.equals("nearestValue")) {
            this.nearestValue = this.parseBoolean(value);
        } else if (keyName.equals("current")) {
            this.current = this.parseBoolean(value);
        }
    }
}
