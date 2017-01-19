/*
 * Copyright (c) 2017 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.ogc.wmts;

import gov.nasa.worldwind.util.xml.XmlModel;

public class OwsServiceIdentification extends XmlModel {

    protected String title;

    protected String serviceAbstract;

    protected OwsKeywords keywords;

    protected String serviceType;

    protected String serviceTypeVersion;

    protected String fees;

    protected String accessConstraints;

    @Override
    protected void parseField(String keyName, Object value) {
        if (keyName.equals("Title")) {
            this.title = (String) value;
        } else if (keyName.equals("Abstract")) {
            this.serviceAbstract = (String) value;
        } else if (keyName.equals("Keywords")) {
            this.keywords = (OwsKeywords) value;
        } else if (keyName.equals("ServiceType")) {
            this.serviceType = (String) value;
        } else if (keyName.equals("ServiceTypeVersion")) {
            this.serviceTypeVersion = (String) value;
        } else if (keyName.equals("Fees")) {
            this.fees = (String) value;
        } else if (keyName.equals("AccessConstraints")) {
            this.accessConstraints = (String) value;
        }
    }
}
