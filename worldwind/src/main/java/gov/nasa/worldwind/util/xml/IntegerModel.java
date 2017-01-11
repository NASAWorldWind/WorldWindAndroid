/*
 * Copyright (c) 2016 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.util.xml;

public class IntegerModel extends XmlModel {

    public IntegerModel(String namespaceUri) {
        super(namespaceUri);
    }

    public Integer getValue() {

        String textValue = this.getField(XmlModel.CHARACTERS_CONTENT).toString();

        if (textValue == null) {
            return null;
        } else {
            try {
                return Integer.parseInt(textValue);
            } catch (NumberFormatException ignore) {

            }
        }

        return null;
    }
}
