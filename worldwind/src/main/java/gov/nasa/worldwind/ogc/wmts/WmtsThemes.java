/*
 * Copyright (c) 2017 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.ogc.wmts;

import java.util.LinkedHashSet;
import java.util.Set;

import gov.nasa.worldwind.util.xml.XmlModel;

public class WmtsThemes extends XmlModel {

    protected Set<WmtsTheme> themes = new LinkedHashSet<>();

    @Override
    protected void parseField(String keyName, Object value) {
        if (keyName.equals("Theme")) {
            this.themes.add((WmtsTheme) value);
        }
    }
}
