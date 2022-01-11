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

    protected final List<String> values = new ArrayList<>();

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
        switch (keyName) {
            case "Identifier":
                this.identifier = (String) value;
                break;
            case "UOM":
                this.unitOfMeasure = (String) value;
                break;
            case "UnitSymbol":
                this.unitSymbol = (String) value;
                break;
            case "Default":
                this.valueDefault = (String) value;
                break;
            case "Current":
                this.current = Boolean.parseBoolean((String) value);
                break;
            case "Value":
                this.values.add((String) value);
                break;
        }
    }
}
