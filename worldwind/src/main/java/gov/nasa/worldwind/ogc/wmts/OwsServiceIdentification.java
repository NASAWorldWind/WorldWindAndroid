/*
 * Copyright (c) 2017 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.ogc.wmts;

import java.util.ArrayList;
import java.util.List;

import gov.nasa.worldwind.util.xml.XmlModel;

public class OwsServiceIdentification extends XmlModel {

    protected String title;

    protected String serviceAbstract;

    protected List<String> keywords = new ArrayList<>();

    protected String serviceType;

    protected List<String> serviceTypeVersions = new ArrayList<>();

    protected List<String> profiles = new ArrayList<>();

    protected String fees;

    protected List<String> accessConstraints = new ArrayList<>();

    public String getTitle() {
        return this.title;
    }

    public String getServiceAbstract() {
        return this.serviceAbstract;
    }

    public List<String> getKeywords() {
        return this.keywords;
    }

    public String getServiceType() {
        return this.serviceType;
    }

    public List<String> getServiceTypeVersions() {
        return this.serviceTypeVersions;
    }

    public String getFees() {
        return this.fees;
    }

    public List<String> getAccessConstraints() {
        return this.accessConstraints;
    }

    @Override
    protected void parseField(String keyName, Object value) {
        if (keyName.equals("Title")) {
            this.title = (String) value;
        } else if (keyName.equals("Abstract")) {
            this.serviceAbstract = (String) value;
        } else if (keyName.equals("Keywords")) {
            this.keywords.addAll(((OwsKeywords) value).getKeywords());
        } else if (keyName.equals("ServiceType")) {
            this.serviceType = (String) value;
        } else if (keyName.equals("ServiceTypeVersion")) {
            this.serviceTypeVersions.add((String) value);
        } else if (keyName.equals("Fees")) {
            this.fees = (String) value;
        } else if (keyName.equals("AccessConstraints")) {
            this.accessConstraints.add((String) value);
        } else if (keyName.equals("Profile")) {
            this.profiles.add((String) value);
        }
    }
}
