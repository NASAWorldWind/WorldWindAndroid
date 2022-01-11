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

    protected final List<WmsLogoUrl> legendUrl = new ArrayList<>();

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
        switch (keyName) {
            case "Name":
                this.name = (String) value;
                break;
            case "Title":
                this.title = (String) value;
                break;
            case "Abstract":
                this.description = (String) value;
                break;
            case "LegendURL":
                this.legendUrl.add((WmsLogoUrl) value);
                break;
            case "StyleSheetURL":
                this.styleSheetUrl = (WmsInfoUrl) value;
                break;
            case "StyleURL":
                this.styleUrl = (WmsInfoUrl) value;
                break;
        }
    }
}
