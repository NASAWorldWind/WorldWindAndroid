/*
 * Copyright (c) 2016 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.util.xml;

public class DoubleModel extends XmlModel {

    public DoubleModel(String namespaceUri) {
        super(namespaceUri);
    }

    public Double getValue() {

        String textValue = this.getField(XmlModel.CHARACTERS_CONTENT).toString();

        if (textValue == null) {
            return null;
        } else {
            try {
                return Double.parseDouble(textValue);
            } catch (NumberFormatException ignore) {

            }
        }

        return null;
    }
}
