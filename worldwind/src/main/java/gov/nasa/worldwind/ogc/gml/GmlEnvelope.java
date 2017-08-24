/*
 * Copyright (c) 2017 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.ogc.gml;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import gov.nasa.worldwind.util.xml.XmlModel;

public class GmlEnvelope extends XmlModel {

    protected GmlDirectPosition lowerCorner;

    protected GmlDirectPosition upperCorner;

    protected String srsName;

    protected String srsDimension;

    protected List<String> axisLabels = Collections.emptyList();

    protected List<String> uomLabels = Collections.emptyList();

    public GmlEnvelope() {
    }

    public GmlDirectPosition getLowerCorner() {
        return lowerCorner;
    }

    public GmlDirectPosition getUpperCorner() {
        return upperCorner;
    }

    public String getSrsName() {
        return srsName;
    }

    public String getSrsDimension() {
        return srsDimension;
    }

    public List<String> getAxisLabels() {
        return axisLabels;
    }

    public List<String> getUomLabels() {
        return uomLabels;
    }

    @Override
    protected void parseField(String keyName, Object value) {
        super.parseField(keyName, value);

        switch (keyName) {
            case "lowerCorner":
                lowerCorner = (GmlDirectPosition) value;
                break;
            case "upperCorner":
                upperCorner = (GmlDirectPosition) value;
                break;
            case "srsName":
                srsName = (String) value;
                break;
            case "srsDimension":
                srsDimension = (String) value;
                break;
            case "axisLabels":
                axisLabels = Arrays.asList(value.toString().split(" "));
                break;
            case "uomLabels":
                uomLabels = Arrays.asList(value.toString().split(" "));
                break;
        }
    }
}
