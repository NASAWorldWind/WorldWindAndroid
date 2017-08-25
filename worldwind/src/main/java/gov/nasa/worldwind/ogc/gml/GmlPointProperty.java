/*
 * Copyright (c) 2017 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.ogc.gml;

import java.util.ArrayList;
import java.util.List;

import gov.nasa.worldwind.util.xml.XmlModel;

public class GmlPointProperty extends XmlModel {

    protected List<GmlPoint> points = new ArrayList<>();

    protected String nilReason;

    public GmlPointProperty() {
    }

    public List<GmlPoint> getPoints() {
        return points;
    }

    public String getNilReason() {
        return nilReason;
    }

    @Override
    protected void parseField(String keyName, Object value) {
        super.parseField(keyName, value);

        switch (keyName) {
            case "Point":
                points.add((GmlPoint) value);
                break;
            case "nilReason":
                nilReason = (String) value;
                break;
        }
    }
}
