/*
 * Copyright (c) 2017 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.ogc.wmts;

import java.util.ArrayList;
import java.util.List;

public class OwsServiceIdentification extends OwsDescription {

    protected String serviceType;

    protected List<String> serviceTypeVersions = new ArrayList<>();

    protected List<String> profiles = new ArrayList<>();

    protected String fees;

    protected List<String> accessConstraints = new ArrayList<>();

    public OwsServiceIdentification() {
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
        super.parseField(keyName, value);
        if (keyName.equals("ServiceType")) {
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
