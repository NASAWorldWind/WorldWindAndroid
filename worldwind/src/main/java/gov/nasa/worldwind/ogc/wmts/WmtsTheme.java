/*
 * Copyright (c) 2017 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.ogc.wmts;

import java.util.ArrayList;
import java.util.List;

public class WmtsTheme extends OwsDescription {

    protected String identifier;

    protected final List<WmtsTheme> themes = new ArrayList<>();

    protected final List<String> layerRefs = new ArrayList<>();

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
        switch (keyName) {
            case "Identifier":
                this.identifier = (String) value;
                break;
            case "Theme":
                this.themes.add((WmtsTheme) value);
                break;
            case "LayerRef":
                this.layerRefs.add((String) value);
                break;
        }
    }
}
