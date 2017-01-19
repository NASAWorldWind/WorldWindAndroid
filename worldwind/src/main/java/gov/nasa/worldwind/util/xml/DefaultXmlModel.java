/*
 * Copyright (c) 2017 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.util.xml;

import java.util.HashMap;
import java.util.Map;

public class DefaultXmlModel extends XmlModel {

    protected Map<String, Object> fields = new HashMap<>();

    protected String text;

    public DefaultXmlModel() {
    }

    public Object getField(String keyName) {
        return this.fields.get(keyName);
    }

    public String getText() {
        return this.text;
    }

    @Override
    protected void parseField(String keyName, Object value) {
        this.fields.put(keyName, value);
    }

    @Override
    protected void parseText(String text) {
        this.text = text;
    }
}
