/*
 * Copyright (c) 2017 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.ogc.wmts;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import gov.nasa.worldwind.util.xml.XmlModel;

public class WmtsTheme extends XmlModel {

    protected String title;

    protected String themeAbstract;

    protected String identifier;

    protected List<WmtsTheme> themes = new ArrayList<>();

    protected List<String> layerRefs = new ArrayList<>();

    public String getTitle() {
        return title;
    }

    public String getThemeAbstract() {
        return this.themeAbstract;
    }

    public String getIdentifier() {
        return this.identifier;
    }

    public List<WmtsTheme> getThemes() {
        return Collections.unmodifiableList(this.themes);
    }

    public List<String> getLayerRefs() {
        return Collections.unmodifiableList(this.layerRefs);
    }

    @Override
    protected void parseField(String keyName, Object value) {
        if (keyName.equals("Title")) {
            this.title = (String) value;
        } else if (keyName.equals("Abstract")) {
            this.themeAbstract = (String) value;
        } else if (keyName.equals("Identifier")) {
            this.identifier = (String) value;
        } else if (keyName.equals("Theme")) {
            this.themes.add((WmtsTheme) value);
        } else if (keyName.equals("LayerRef")) {
            this.layerRefs.add((String) value);
        }
    }
}
