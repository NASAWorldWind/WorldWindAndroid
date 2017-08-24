/*
 * Copyright (c) 2017 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.ogc.gml;

import gov.nasa.worldwind.util.xml.XmlModel;

public class GmlGridLimits extends XmlModel {

    protected GmlGridEnvelope gridEnvelope;

    public GmlGridLimits() {
    }

    public GmlGridEnvelope getGridEnvelope() {
        return gridEnvelope;
    }

    @Override
    protected void parseField(String keyName, Object value) {
        super.parseField(keyName, value);

        switch (keyName) {
            case "GridEnvelope":
                gridEnvelope = (GmlGridEnvelope) value;
                break;
        }
    }
}
