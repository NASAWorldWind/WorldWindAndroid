/*
 * Copyright (c) 2017 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.ogc.wms;

import gov.nasa.worldwind.util.xml.XmlModel;

public class WmsDimension extends XmlModel {

    protected String name;

    protected String units;

    protected String unitSymbol;

    protected String defaultValue;

    protected Boolean multipleValues;

    protected Boolean nearestValue;

    protected Boolean current;

    public WmsDimension() {
    }

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

    @Override
    public void parseField(String keyName, Object value) {
        if (keyName.equals("name")) {
            this.name = (String) value;
        } else if (keyName.equals("units")) {
            this.units = (String) value;
        } else if (keyName.equals("unitSymbol")) {
            this.unitSymbol = (String) value;
        } else if (keyName.equals("default")) {
            this.defaultValue = (String) value;
        } else if (keyName.equals("multipleValues")) {
            this.multipleValues = Boolean.parseBoolean((String) value);
        } else if (keyName.equals("nearestValue")) {
            this.nearestValue = Boolean.parseBoolean((String) value);
        } else if (keyName.equals("current")) {
            this.current = Boolean.parseBoolean((String) value);
        }
    }
}
