/*
 * Copyright (c) 2017 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.ogc.wmts;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import gov.nasa.worldwind.util.xml.XmlModel;

public class WmtsDimension extends XmlModel {

    protected String identifier;

    protected String title;

    protected String dimensionAbstract;

    protected List<String> kewords = new ArrayList<>();

    protected String unitOfMeasure;

    protected String unitSymbol;

    protected String valueDefault;

    protected Boolean current;

    protected List<String> values = new ArrayList<>();

    public String getIdentifier() {
        return this.identifier;
    }

    public String getTitle() {
        return this.title;
    }

    public String getDimensionAbstract() {
        return this.dimensionAbstract;
    }

    public List<String> getKewords() {
        return Collections.unmodifiableList(this.kewords);
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
        return Collections.unmodifiableList(this.values);
    }

    @Override
    protected void parseField(String keyName, Object value) {
        if (keyName.equals("Identifier")) {
            this.identifier = (String) value;
        } else if (keyName.equals("Title")) {
            this.title = (String) value;
        } else if (keyName.equals("Abstract")) {
            this.dimensionAbstract = (String) value;
        } else if (keyName.equals("Keywords")) {
            this.kewords.addAll(((OwsKeywords) value).getKeywords());
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
