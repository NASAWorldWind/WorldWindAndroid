/*
 * Copyright (c) 2017 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.ogc.wms;

import java.util.Collections;
import java.util.Set;

import gov.nasa.worldwind.util.Logger;
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

    public WmsServiceInformation(String namespaceUri) {
        super(namespaceUri);
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
    public void setField(String keyName, Object value) {
        if (keyName.equals("Name")) {
            this.name = ((XmlModel) value).getCharactersContent();
        } else if (keyName.equals("Title")) {
            this.title = ((XmlModel) value).getCharactersContent();
        } else if (keyName.equals("Abstract")) {
            this.description = ((XmlModel) value).getCharactersContent();
        } else if (keyName.equals("KeywordList")) {
            this.keywords = (WmsKeywords) value;
        } else if (keyName.equals("OnlineResource")) {
            this.onlineResource = (WmsOnlineResource) value;
        } else if (keyName.equals("ContactInformation")) {
            this.contactInformation = (WmsContactInformation) value;
        } else if (keyName.equals("Fees")) {
            this.fees = ((XmlModel) value).getCharactersContent();
        } else if (keyName.equals("AccessConstraints")) {
            this.accessConstraints = ((XmlModel) value).getCharactersContent();
        } else if (keyName.equals("MaxWidth")) {
            this.maxWidth = this.parseInt(value);
        } else if (keyName.equals("MaxHeight")) {
            this.maxHeight = this.parseInt(value);
        } else if (keyName.equals("LayerLimit")) {
            this.layerLimit = this.parseInt(value);
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        sb.append("ServiceName: ").append(this.getServiceName() != null ? this.getServiceName() : "none").append("\n");
        sb.append("ServiceTitle: ").append(this.getServiceTitle() != null ? this.getServiceTitle() : "none").append("\n");
        sb.append("ServiceAbstract: ").append(this.getServiceAbstract() != null ? this.getServiceAbstract() : "none").append(
            "\n");
        sb.append("Fees: ").append(this.getFees() != null ? this.getFees() : "none").append("\n");
        sb.append("AccessConstraints: ").append(
            this.getAccessConstraints() != null ? this.getAccessConstraints() : "none").append("\n");
        this.keywordsToString(sb);
        sb.append("OnlineResource: ").append(this.getOnlineResource() != null ? this.getOnlineResource() : "none").append("\n");
        sb.append(this.getContactInformation() != null ? this.getContactInformation() : "none").append("\n");
        sb.append("Max width = ").append(this.getMaxWidth());
        sb.append(" Max height = ").append(this.getMaxHeight()).append("\n");

        return sb.toString();
    }

    protected void keywordsToString(StringBuilder sb) {
        sb.append("Keywords: ");
        if (this.getKeywords().size() == 0)
            sb.append(" none");
        else {
            for (String keyword : this.getKeywords()) {
                sb.append(keyword != null ? keyword : "null").append(", ");
            }
        }
        sb.append("\n");
    }

    protected Integer parseInt(Object value) {
        try {
            return Integer.parseInt(((XmlModel) value).getCharactersContent());
        } catch (Exception e) {
            Logger.makeMessage("WmsServiceInformation", "parseInt", e.toString());
        }
        return null;
    }
}
