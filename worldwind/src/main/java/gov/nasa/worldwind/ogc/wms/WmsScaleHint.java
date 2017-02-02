/*
 * Copyright (c) 2017 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.ogc.wms;

import gov.nasa.worldwind.util.xml.XmlModel;

public class WmsScaleHint extends XmlModel {

    protected Double min;

    protected Double max;

    public WmsScaleHint() {
    }

    public Double getMin() {
        return this.min;
    }

    public Double getMax() {
        return this.max;
    }

    @Override
    protected void parseField(String keyName, Object value) {
        if (keyName.equals("min")) {
            this.min = Double.parseDouble((String) value);
        } else if (keyName.equals("max")) {
            this.max = Double.parseDouble((String) value);
        }
    }
}
