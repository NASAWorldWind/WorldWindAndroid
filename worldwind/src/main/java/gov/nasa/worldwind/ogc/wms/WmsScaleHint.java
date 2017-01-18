/*
 * Copyright (c) 2017 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.ogc.wms;

import gov.nasa.worldwind.util.Logger;
import gov.nasa.worldwind.util.xml.XmlModel;

public class WmsScaleHint extends XmlModel {

    protected Double min;

    protected Double max;

    public WmsScaleHint() {}

    protected Double parseDouble(Object value) {
        try {
            return Double.parseDouble(value.toString());
        } catch (NumberFormatException ex) {
            Logger.logMessage(Logger.ERROR, "WmsScaleHint", "parseDouble", "Parse error: " + ex.toString());
        }
        return null;
    }

    @Override
    protected void setField(String keyName, Object value) {
        if (keyName.equals("min")) {
            this.min = this.parseDouble(value);
        } else if (keyName.equals("max")) {
            this.max = this.parseDouble(value);
        }
    }
}
