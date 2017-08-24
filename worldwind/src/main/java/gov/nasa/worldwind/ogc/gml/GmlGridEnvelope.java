/*
 * Copyright (c) 2017 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.ogc.gml;

import gov.nasa.worldwind.util.xml.XmlModel;

public class GmlGridEnvelope extends XmlModel {

    protected GmlIntegerList low = new GmlIntegerList();

    protected GmlIntegerList high = new GmlIntegerList();

    public GmlGridEnvelope() {
    }

    public GmlIntegerList getLow() {
        return low;
    }

    public GmlIntegerList getHigh() {
        return high;
    }

    @Override
    protected void parseField(String keyName, Object value) {
        super.parseField(keyName, value);

        switch (keyName) {
            case "low":
                this.low = (GmlIntegerList) value;
                break;
            case "high":
                this.high = (GmlIntegerList) value;
                break;
        }
    }
}
