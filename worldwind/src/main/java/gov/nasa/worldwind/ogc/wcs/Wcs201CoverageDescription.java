/*
 * Copyright (c) 2017 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.ogc.wcs;

import gov.nasa.worldwind.ogc.gml.GmlAbstractFeature;
import gov.nasa.worldwind.ogc.gml.GmlDomainSet;

public class Wcs201CoverageDescription extends GmlAbstractFeature {

    protected String coverageId;

    protected GmlDomainSet domainSet;

    public Wcs201CoverageDescription() {
    }

    public String getCoverageId() {
        return this.coverageId;
    }

    @Override
    protected void parseField(String keyName, Object value) {
        super.parseField(keyName, value);

        if (keyName.equals("CoverageId")) {
            this.coverageId = (String) value;
        } else if (keyName.equals("domainSet")) {
            this.domainSet = (GmlDomainSet) value;
        }
    }
}
