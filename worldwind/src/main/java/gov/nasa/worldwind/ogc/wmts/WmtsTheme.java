/*
 * Copyright (c) 2017 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.ogc.wmts;

import java.util.ArrayList;
import java.util.List;

public class WmtsTheme extends OwsDescription {

    protected String identifier;

    protected List<WmtsTheme> themes = new ArrayList<>();

    protected List<String> layerRefs = new ArrayList<>();

    public WmtsTheme() {
    }

    public String getIdentifier() {
        return this.identifier;
    }

    public List<WmtsTheme> getThemes() {
        return this.themes;
    }

    public List<String> getLayerRefs() {
        return this.layerRefs;
    }

    @Override
    protected void parseField(String keyName, Object value) {
        super.parseField(keyName, value);
        if (keyName.equals("Identifier")) {
            this.identifier = (String) value;
        } else if (keyName.equals("Theme")) {
            this.themes.add((WmtsTheme) value);
        } else if (keyName.equals("LayerRef")) {
            this.layerRefs.add((String) value);
        }
    }
}
