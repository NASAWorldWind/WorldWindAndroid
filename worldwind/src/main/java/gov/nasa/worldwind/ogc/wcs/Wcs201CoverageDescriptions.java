/*
 * Copyright (c) 2017 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.ogc.wcs;

import java.util.ArrayList;
import java.util.List;

import gov.nasa.worldwind.util.xml.XmlModel;

public class Wcs201CoverageDescriptions extends XmlModel {

    protected List<Wcs201CoverageDescription> coverageDescriptions = new ArrayList<>();

    public Wcs201CoverageDescriptions() {
    }

    public List<Wcs201CoverageDescription> getCoverageDescriptions() {
        return coverageDescriptions;
    }

    public Wcs201CoverageDescription getCoverageDescription(String identifier) {
        for (Wcs201CoverageDescription coverageDescription : coverageDescriptions) {
            if (coverageDescription.getCoverageId().equals(identifier)) {
                return coverageDescription;
            }
        }

        return null;
    }

    @Override
    protected void parseField(String keyName, Object value) {
        super.parseField(keyName, value);

        switch (keyName) {
            case "CoverageDescription":
                coverageDescriptions.add((Wcs201CoverageDescription) value);
                break;
        }
    }
}
