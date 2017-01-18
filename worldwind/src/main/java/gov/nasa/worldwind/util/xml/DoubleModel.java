/*
 * Copyright (c) 2016 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.util.xml;

import gov.nasa.worldwind.util.Logger;

public class DoubleModel extends XmlModel {

    protected Double value;

    public DoubleModel() {
    }

    public Double getValue() {
        return this.value;
    }

    @Override
    protected void setText(String value) {
        try {
            this.value = Double.parseDouble(value);
        } catch (NumberFormatException ex) {
            Logger.logMessage(Logger.ERROR, "DoubleModel", "setField", "Parsing error " + ex.toString());
        }
    }

    //    @Override
//    protected void setField(String keyName, Object value) {
//        if (keyName.equals(XmlModel.CHARACTERS_CONTENT)) {
//            try {
//                this.value = Double.parseDouble(value.toString());
//            } catch (NumberFormatException ex) {
//                Logger.logMessage(Logger.ERROR, "DoubleModel", "setField", "Parsing error " + ex.toString());
//            }
//        }
//    }
}
