/*
 * Copyright (c) 2017 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.util.xml;

import java.text.NumberFormat;
import java.text.ParseException;

import gov.nasa.worldwind.util.Logger;

public class NumberModel extends XmlModel {

    protected StringBuilder text = new StringBuilder();

    public NumberModel() {

    }

    public Number getValue() {
        try {
            return NumberFormat.getInstance().parse(this.text.toString());
        } catch (ParseException ex) {
            Logger.logMessage(Logger.ERROR, "NumberModel", "parseField",
                "Exception parsing number '" + this.text + "'", ex);
            return null;
        }
    }

    @Override
    protected void parseText(String text) {
        if (text == null || text.isEmpty()) {
            return; // nothing to parse
        }

        text = text.replaceAll("\n", "").trim();
        if (text.isEmpty()) {
            return; // nothing but whitespace
        }

        this.text.append(text);
    }
}
