/*
 * Copyright (c) 2017 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.ogc.gml;

import gov.nasa.worldwind.util.xml.XmlModel;

public class GmlBoundingShape extends XmlModel {

    protected GmlEnvelope envelope;

    protected String nilReason;

    public GmlBoundingShape() {
    }

    public GmlEnvelope getEnvelope() {
        return envelope;
    }

    public String getNilReason() {
        return nilReason;
    }

    @Override
    protected void parseField(String keyName, Object value) {
        super.parseField(keyName, value);

        switch (keyName) {
            case "Envelope":
                envelope = (GmlEnvelope) value;
                break;
            case "nilReason":
                nilReason = (String) value;
                break;
        }
    }
}
