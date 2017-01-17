/*
 * Copyright (c) 2017 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.ogc.wms;

import java.util.LinkedHashSet;
import java.util.Set;

import gov.nasa.worldwind.util.xml.TextModel;
import gov.nasa.worldwind.util.xml.XmlModel;

public class WmsLayerStyle extends XmlModel {

    protected String name;

    protected String title;

    protected String description;

    protected Set<WmsLogoUrl> legendUrl = new LinkedHashSet<>();

    protected WmsLayerInfoUrl styleSheetUrl;

    protected WmsLayerInfoUrl styleUrl;

    public WmsLayerStyle(String namespaceURI) {
        super(namespaceURI);
    }

    public String getName() {
        return name;
    }

    public String getTitle() {
        return title;
    }

    public String getAbstract() {
        return description;
    }

    public Set<WmsLogoUrl> getLegendUrls() {
        return legendUrl;
    }

    public WmsLayerInfoUrl getStyleSheetUrl() {
        return styleSheetUrl;
    }

    public WmsLayerInfoUrl getStyleUrl() {
        return styleUrl;
    }

    @Override
    public void setField(String keyName, Object value) {
        if (keyName.equals("Name")) {
            this.name = ((TextModel) value).getValue();
        } else if (keyName.equals("Title")) {
            this.title = ((TextModel) value).getValue();
        } else if (keyName.equals("Abstract")) {
            this.description = ((TextModel) value).getValue();
        } else if (keyName.equals("LegendURL")) {
            this.legendUrl.add((WmsLogoUrl) value);
        } else if (keyName.equals("StyleSheetURL")) {
            this.styleSheetUrl = (WmsLayerInfoUrl) value;
        } else if (keyName.equals("StyleURL")) {
            this.styleUrl = (WmsLayerInfoUrl) value;
        }
    }
}
