/*
 * Copyright (c) 2017 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.ogc.wmts;

import java.util.ArrayList;
import java.util.List;

public class WmtsStyle extends OwsDescription {

    protected String identifier;

    protected boolean isDefault = false;

    protected List<WmtsElementLink> legendUrls = new ArrayList<>();

    public WmtsStyle() {
    }

    public String getIdentifier() {
        return this.identifier;
    }

    public boolean isDefault() {
        return this.isDefault;
    }

    public List<WmtsElementLink> getLegendUrls() {
        return this.legendUrls;
    }

    @Override
    protected void parseField(String keyName, Object value) {
        super.parseField(keyName, value);
        if (keyName.equals("Identifier")) {
            this.identifier = (String) value;
        } else if (keyName.equals("isDefault")) {
            this.isDefault = Boolean.parseBoolean((String) value);
        } else if (keyName.equals("LegendURL")) {
            this.legendUrls.add((WmtsElementLink) value);
        }
    }
}
