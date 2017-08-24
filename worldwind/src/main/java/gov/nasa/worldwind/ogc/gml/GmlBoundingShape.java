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
        return this.envelope;
    }

    public String getNilReason() {
        return this.nilReason;
    }

    @Override
    protected void parseField(String keyName, Object value) {
        super.parseField(keyName, value);

        if (keyName.equals("Envelope")) {
            this.envelope = (GmlEnvelope) value;
        } else if (keyName.equals("nilReason")) {
            this.nilReason = (String) value;
        }
    }
}
