/*
 * Copyright (c) 2017 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.ogc.wms;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

import gov.nasa.worldwind.util.xml.XmlModel;

public class WmsException extends XmlModel {

    protected Set<String> exceptionFormats = new LinkedHashSet<>();

    public WmsException() {
    }

    public Set<String> getExceptionFormats() {
        return Collections.unmodifiableSet(this.exceptionFormats);
    }

    @Override
    public void parseField(String keyName, Object value) {
        if (keyName.equals("Format")) {
            this.exceptionFormats.add((String) value);
        }
    }
}
