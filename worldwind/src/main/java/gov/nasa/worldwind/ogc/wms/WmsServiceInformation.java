/*
 * Copyright (c) 2016 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.ogc.wms;

import java.util.Set;

import javax.xml.namespace.QName;

import gov.nasa.worldwind.util.xml.IntegerModel;
import gov.nasa.worldwind.util.xml.XmlModel;

public class WmsServiceInformation extends XmlModel {

    protected QName name;

    protected QName title;

    protected QName abstractDescription;

    protected QName fees;

    protected QName accessConstraints;

    protected QName keywordList;

    protected QName keyword;

    protected QName onlineResource;

    protected QName contactInformation;

    protected QName maxWidth;

    protected QName maxHeight;

    protected QName layerLimit;

    public WmsServiceInformation(String namespaceUri) {
        super(namespaceUri);

        this.initialize();
    }

    private void initialize() {
        this.name = new QName(this.getNamespaceUri(), "Name");
        this.title = new QName(this.getNamespaceUri(), "Title");
        this.abstractDescription = new QName(this.getNamespaceUri(), "Abstract");
        this.fees = new QName(this.getNamespaceUri(), "Fees");
        this.accessConstraints = new QName(this.getNamespaceUri(), "AccessConstraints");
        this.keywordList = new QName(this.getNamespaceUri(), "KeywordList");
        this.keyword = new QName(this.getNamespaceUri(), "Keyword");
        this.onlineResource = new QName(this.getNamespaceUri(), "OnlineResource");
        this.contactInformation = new QName(this.getNamespaceUri(), "ContactInformation");
        this.maxWidth = new QName(this.getNamespaceUri(), "MaxWidth");
        this.maxHeight = new QName(this.getNamespaceUri(), "MaxHeight");
        this.layerLimit = new QName(this.getNamespaceUri(), "LayerLimit");
    }

    public WmsContactInformation getContactInformation() {
        return (WmsContactInformation) this.getField(this.contactInformation);
    }

    protected void setContactInformation(WmsContactInformation contactInformation) {
        this.setField(this.contactInformation, contactInformation);
    }

    public WmsOnlineResource getOnlineResource() {
        return (WmsOnlineResource) this.getField(this.onlineResource);
    }

    protected void setOnlineResource(WmsOnlineResource onlineResource) {
        this.setField(this.onlineResource, onlineResource);
    }

    public Set<String> getKeywords() {
        return ((WmsKeywords) this.getField(this.keywordList)).getKeywords();
    }

//    protected void setKeywords(Set<String> keywords) {
//        this.keywords = keywords;
//    }

    public String getAccessConstraints() {
        return this.getChildCharacterValue(this.accessConstraints);
    }

    protected void setAccessConstraints(String accessConstraints) {
        this.setChildCharacterValue(this.accessConstraints, accessConstraints);
    }

    public String getFees() {
        return this.getChildCharacterValue(this.fees);
    }

    protected void setFees(String fees) {
        this.setChildCharacterValue(this.fees, fees);
    }

    public String getServiceAbstract() {
        return this.getChildCharacterValue(this.abstractDescription);
    }

    protected void setServiceAbstract(String serviceAbstract) {
        this.setChildCharacterValue(this.abstractDescription, serviceAbstract);
    }

    public String getServiceTitle() {
        return this.getChildCharacterValue(this.title);
    }

    protected void setServiceTitle(String serviceTitle) {
        this.setChildCharacterValue(this.title, serviceTitle);
    }

    public String getServiceName() {
        return this.getChildCharacterValue(this.name);
    }

    protected void setServiceName(String serviceName) {
        this.setChildCharacterValue(this.name, serviceName);
    }

    public int getMaxWidth() {
        Integer value = ((IntegerModel) this.getField(this.maxWidth)).getValue();
        return value != null ? value : 0;
    }

//    protected void setMaxWidth(int maxWidth) {
//        this.maxWidth = maxWidth;
//    }

    public int getMaxHeight() {
        Integer value = ((IntegerModel) this.getField(this.maxHeight)).getValue();
        return value != null ? value : 0;
    }

//    protected void setMaxHeight(int maxHeight) {
//        this.maxHeight = maxHeight;
//    }

    public int getLayerLimit() {
        Integer value = ((IntegerModel) this.getField(this.layerLimit)).getValue();
        return value != null ? value : 0;
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
}
