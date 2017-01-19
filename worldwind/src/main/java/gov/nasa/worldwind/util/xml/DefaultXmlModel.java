/*
 * Copyright (c) 2017 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.util.xml;

import java.util.HashMap;
import java.util.Map;

public class DefaultXmlModel extends XmlModel {

    protected Map<String, Object> fields;

    protected StringBuilder text;

    public DefaultXmlModel() {
    }

    public Object getField(String keyName) {
        return (this.fields != null) ? this.fields.get(keyName) : null;
    }

    public String getText() {
        return (this.text != null) ? this.text.toString() : null;
    }

    @Override
    protected void parseField(String keyName, Object value) {
        if (this.fields == null) {
            this.fields = new HashMap<>();
        }
        this.fields.put(keyName, value);
    }

    @Override
    protected void parseText(String text) {
        if (text == null) {
            return; // nothing to parse
        }

        if (this.text == null) {
            this.text = new StringBuilder();
        }
        this.text.append(text);
    }
}
