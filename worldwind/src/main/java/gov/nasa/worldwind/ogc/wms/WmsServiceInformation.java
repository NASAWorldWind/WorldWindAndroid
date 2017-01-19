/*
 * Copyright (c) 2017 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.ogc.wms;

import java.util.Collections;
import java.util.Set;

import gov.nasa.worldwind.util.xml.XmlModel;

public class WmsServiceInformation extends XmlModel {

    protected String name;

    protected String title;

    protected String description;

    protected String fees;

    protected String accessConstraints;

    protected WmsKeywords keywords;

    protected WmsOnlineResource onlineResource;

    protected WmsContactInformation contactInformation;

    protected Integer maxWidth;

    protected Integer maxHeight;

    protected Integer layerLimit;

    public WmsServiceInformation() {
    }

    public WmsContactInformation getContactInformation() {
        return this.contactInformation;
    }

    public WmsOnlineResource getOnlineResource() {
        return this.onlineResource;
    }

    public Set<String> getKeywords() {
        if (this.keywords != null) {
            return this.keywords.getKeywords();
        } else {
            return Collections.emptySet();
        }
    }

    public String getAccessConstraints() {
        return this.accessConstraints;
    }

    public String getFees() {
        return this.fees;
    }

    public String getServiceAbstract() {
        return this.description;
    }

    public String getServiceTitle() {
        return this.title;
    }

    public String getServiceName() {
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
            this.keywords = (WmsKeywords) value;
        } else if (keyName.equals("OnlineResource")) {
            this.onlineResource = (WmsOnlineResource) value;
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
