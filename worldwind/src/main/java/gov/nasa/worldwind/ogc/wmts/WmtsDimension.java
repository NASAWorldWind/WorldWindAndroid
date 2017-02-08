/*
 * Copyright (c) 2017 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.ogc.wmts;

import java.util.ArrayList;
import java.util.List;

public class WmtsDimension extends OwsDescription {

    protected String identifier;

    protected String unitOfMeasure;

    protected String unitSymbol;

    protected String valueDefault;

    protected Boolean current;

    protected List<String> values = new ArrayList<>();

    public WmtsDimension() {
    }

    public String getIdentifier() {
        return this.identifier;
    }

    public String getUnitOfMeasure() {
        return this.unitOfMeasure;
    }

    public String getUnitSymbol() {
        return this.unitSymbol;
    }

    public String getValueDefault() {
        return this.valueDefault;
    }

    public Boolean getCurrent() {
        return this.current;
    }

    public List<String> getValues() {
        return this.values;
    }

    @Override
    protected void parseField(String keyName, Object value) {
        super.parseField(keyName, value);
        if (keyName.equals("Identifier")) {
            this.identifier = (String) value;
        } else if (keyName.equals("UOM")) {
            this.unitOfMeasure = (String) value;
        } else if (keyName.equals("UnitSymbol")) {
            this.unitSymbol = (String) value;
        } else if (keyName.equals("Default")) {
            this.valueDefault = (String) value;
        } else if (keyName.equals("Current")) {
            this.current = Boolean.parseBoolean((String) value);
        } else if (keyName.equals("Value")) {
            this.values.add((String) value);
        }
    }
}
