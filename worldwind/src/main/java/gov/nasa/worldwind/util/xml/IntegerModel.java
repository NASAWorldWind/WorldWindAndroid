/*
 * Copyright (c) 2016 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.util.xml;

import gov.nasa.worldwind.util.Logger;

public class IntegerModel extends XmlModel {

    protected Integer value;

    public IntegerModel() {

    }

    public Integer getValue() {
        return this.value;
    }

    @Override
    protected void setText(String value) {
        try {
            this.value = Integer.parseInt(value);
        } catch (NumberFormatException ex) {
            Logger.logMessage(Logger.ERROR, "IntegerModel", "setField", "Parsing error " + ex.toString());
        }
    }

    //    @Override
//    protected void setField(String keyName, Object value) {
//        if (keyName.equals(XmlModel.CHARACTERS_CONTENT)) {
//            try {
//                this.value = Integer.parseInt(value.toString());
//            } catch (NumberFormatException ex) {
//                Logger.logMessage(Logger.ERROR, "IntegerModel", "setField", "Parsing error " + ex.toString());
//            }
//        }
//    }
}
