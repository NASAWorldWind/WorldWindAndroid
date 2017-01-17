/*
 * Copyright (c) 2017 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.util.xml;

import java.util.HashMap;
import java.util.Map;

public class DefaultXmlModel extends XmlModel {

    protected Map<String, Object> fields = new HashMap<>();

    public DefaultXmlModel(String namespaceUri) {
        super(namespaceUri);
    }

    @Override
    protected void setField(String keyName, Object value) {
        this.fields.put(keyName, value);
    }

    public Object getField(String keyName) {
        return this.fields.get(keyName);
    }
}
