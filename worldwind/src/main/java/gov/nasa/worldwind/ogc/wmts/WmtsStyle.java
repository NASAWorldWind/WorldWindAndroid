/*
 * Copyright (c) 2017 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.ogc.wmts;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import gov.nasa.worldwind.util.xml.XmlModel;

public class WmtsStyle extends XmlModel {

    protected String title;

    protected String identifier;

    protected boolean isDefault = false;

    protected List<WmtsElementLink> legendUrls = new ArrayList<>();

    public String getTitle() {
        return this.title;
    }

    public String getIdentifier() {
        return this.identifier;
    }

    public boolean isDefault() {
        return this.isDefault;
    }

    public List<WmtsElementLink> getLegendUrls() {
        return Collections.unmodifiableList(this.legendUrls);
    }

    @Override
    protected void parseField(String keyName, Object value) {
        if (keyName.equals("Title")) {
            this.title = (String) value;
        } else if (keyName.equals("Identifier")) {
            this.identifier = (String) value;
        } else if (keyName.equals("isDefault")) {
            this.isDefault = Boolean.parseBoolean((String) value);
        } else if (keyName.equals("LegendURL")) {
            this.legendUrls.add((WmtsElementLink) value);
        }
    }
}
