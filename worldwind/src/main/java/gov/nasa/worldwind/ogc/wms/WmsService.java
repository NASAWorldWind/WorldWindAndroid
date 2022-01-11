/*
 * Copyright (c) 2017 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.ogc.wms;

import java.util.ArrayList;
import java.util.List;

import gov.nasa.worldwind.util.xml.XmlModel;

public class WmsService extends XmlModel {

    protected String name;

    protected String title;

    protected String description;

    protected String fees;

    protected String accessConstraints;

    protected final List<String> keywordList = new ArrayList<>();

    protected String url;

    protected WmsContactInformation contactInformation;

    protected Integer maxWidth;

    protected Integer maxHeight;

    protected Integer layerLimit;

    public WmsService() {
    }

    public WmsContactInformation getContactInformation() {
        return this.contactInformation;
    }

    public String getUrl() {
        return this.url;
    }

    public List<String> getKeywordList() {
        return this.keywordList;
    }

    public String getAccessConstraints() {
        return this.accessConstraints;
    }

    public String getFees() {
        return this.fees;
    }

    public String getAbstract() {
        return this.description;
    }

    public String getTitle() {
        return this.title;
    }

    public String getName() {
        return this.name;
    }

    public Integer getMaxWidth() {
        return this.maxWidth;
    }

    public Integer getMaxHeight() {
        return this.maxHeight;
    }

    public Integer getLayerLimit() {
        return this.layerLimit;
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
            case "KeywordList":
                this.keywordList.addAll(((WmsKeywords) value).getKeywords());
                break;
            case "OnlineResource":
                this.url = ((WmsOnlineResource) value).getUrl();
                break;
            case "ContactInformation":
                this.contactInformation = (WmsContactInformation) value;
                break;
            case "Fees":
                this.fees = (String) value;
                break;
            case "AccessConstraints":
                this.accessConstraints = (String) value;
                break;
            case "MaxWidth":
                this.maxWidth = Integer.parseInt((String) value);
                break;
            case "MaxHeight":
                this.maxHeight = Integer.parseInt((String) value);
                break;
            case "LayerLimit":
                this.layerLimit = Integer.parseInt((String) value);
                break;
        }
    }
}
