/*
 * Copyright (c) 2017 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.ogc.gml;

import gov.nasa.worldwind.util.Logger;
import gov.nasa.worldwind.util.xml.XmlModel;

public class GmlDoubleList extends XmlModel {

    protected double[] values = new double[0];

    public GmlDoubleList() {
    }

    public double[] getValues() {
        return values;
    }

    @Override
    protected void parseText(String text) {
        String[] tokens = text.split(" ");
        values = new double[tokens.length];

        for (int idx = 0, len = tokens.length; idx < len; idx++) {
            try {
                values[idx] = Double.parseDouble(tokens[idx]);
            } catch (NumberFormatException e) {
                Logger.logMessage(Logger.ERROR, "GmlDoubleList", "parseText", "exceptionParsingText", e);
            }
        }
    }
}
