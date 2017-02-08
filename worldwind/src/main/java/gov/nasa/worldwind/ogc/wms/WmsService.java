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

    protected List<String> keywordList = new ArrayList<>();

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
        if (keyName.equals("Name")) {
            this.name = (String) value;
        } else if (keyName.equals("Title")) {
            this.title = (String) value;
        } else if (keyName.equals("Abstract")) {
            this.description = (String) value;
        } else if (keyName.equals("KeywordList")) {
            this.keywordList.addAll(((WmsKeywords) value).getKeywords());
        } else if (keyName.equals("OnlineResource")) {
            this.url = ((WmsOnlineResource) value).getUrl();
        } else if (keyName.equals("ContactInformation")) {
            this.contactInformation = (WmsContactInformation) value;
        } else if (keyName.equals("Fees")) {
            this.fees = (String) value;
        } else if (keyName.equals("AccessConstraints")) {
            this.accessConstraints = (String) value;
        } else if (keyName.equals("MaxWidth")) {
            this.maxWidth = Integer.parseInt((String) value);
        } else if (keyName.equals("MaxHeight")) {
            this.maxHeight = Integer.parseInt((String) value);
        } else if (keyName.equals("LayerLimit")) {
            this.layerLimit = Integer.parseInt((String) value);
        }
    }
}
