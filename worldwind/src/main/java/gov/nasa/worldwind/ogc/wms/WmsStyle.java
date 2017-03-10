/*
 * Copyright (c) 2017 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.ogc.wms;

import java.util.ArrayList;
import java.util.List;

import gov.nasa.worldwind.util.xml.XmlModel;

public class WmsStyle extends XmlModel {

    protected String name;

    protected String title;

    protected String description;

    protected List<WmsLogoUrl> legendUrl = new ArrayList<>();

    protected WmsInfoUrl styleSheetUrl;

    protected WmsInfoUrl styleUrl;

    public WmsStyle() {
    }

    public String getName() {
        return this.name;
    }

    public String getTitle() {
        return this.title;
    }

    public String getAbstract() {
        return this.description;
    }

    public List<WmsLogoUrl> getLegendUrls() {
        return this.legendUrl;
    }

    public WmsInfoUrl getStyleSheetUrl() {
        return this.styleSheetUrl;
    }

    public WmsInfoUrl getStyleUrl() {
        return this.styleUrl;
    }

    @Override
    public void parseField(String keyName, Object value) {
        if (keyName.equals("Name")) {
            this.name = (String) value;
        } else if (keyName.equals("Title")) {
            this.title = (String) value;
        } else if (keyName.equals("Abstract")) {
            this.description = (String) value;
        } else if (keyName.equals("LegendURL")) {
            this.legendUrl.add((WmsLogoUrl) value);
        } else if (keyName.equals("StyleSheetURL")) {
            this.styleSheetUrl = (WmsInfoUrl) value;
        } else if (keyName.equals("StyleURL")) {
            this.styleUrl = (WmsInfoUrl) value;
        }
    }
}
