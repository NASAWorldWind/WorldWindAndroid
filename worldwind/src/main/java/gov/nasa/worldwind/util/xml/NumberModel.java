/*
 * Copyright (c) 2017 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.util.xml;

import java.text.NumberFormat;
import java.text.ParseException;

import gov.nasa.worldwind.util.Logger;

public class NumberModel extends XmlModel {

    protected Number value;

    public NumberModel() {

    }

    public Number getValue() {
        return this.value;
    }

    @Override
    protected void setField(String keyName, Object value) {
        if (keyName.equals(CHARACTERS_FIELD)) {
            try {
                this.value = NumberFormat.getInstance().parse(value.toString());
            } catch (ParseException ex) {
                Logger.logMessage(Logger.ERROR, "NumberModel", "setField",
                    "Exception parsing number '" + value + "'", ex);
            }
        }
    }
}
