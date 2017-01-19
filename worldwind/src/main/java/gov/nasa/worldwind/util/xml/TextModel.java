/*
 * Copyright (c) 2017 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.util.xml;

public class TextModel extends XmlModel {

    protected String value;

    public TextModel() {
    }

    public String getValue() {
        return this.value;
    }

    @Override
    protected void setField(String keyName, Object value) {
        if (keyName.equals(CHARACTERS_FIELD)) {
            this.value = value.toString();
        }
    }
}
