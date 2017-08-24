/*
 * Copyright (c) 2017 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.ogc.gml;

import java.util.Arrays;
import java.util.List;

import gov.nasa.worldwind.util.xml.XmlModel;

public class GmlEnvelope extends XmlModel {

    protected GmlDirectPosition lowerCorner;

    protected GmlDirectPosition upperCorner;

    protected String srsName;

    protected String srsDimension;

    protected List<String> axisLabels;

    protected List<String> uomLabels;

    public GmlEnvelope() {
    }

    public GmlDirectPosition getLowerCorner() {
        return this.lowerCorner;
    }

    public GmlDirectPosition getUpperCorner() {
        return this.upperCorner;
    }

    public String getSrsName() {
        return this.srsName;
    }

    public String getSrsDimension() {
        return this.srsDimension;
    }

    public List<String> getAxisLabels() {
        return this.axisLabels;
    }

    public List<String> getUomLabels() {
        return this.uomLabels;
    }

    @Override
    protected void parseField(String keyName, Object value) {
        super.parseField(keyName, value);

        if (keyName.equals("lowerCorner")) {
            this.lowerCorner = (GmlDirectPosition) value;
        } else if (keyName.equals("upperCorner")) {
            this.upperCorner = (GmlDirectPosition) value;
        } else if (keyName.equals("srsName")) {
            this.srsName = (String) value;
        } else if (keyName.equals("srsDimension")) {
            this.srsDimension = (String) value;
        } else if (keyName.equals("axisLabels")) {
            this.axisLabels = Arrays.asList(((String) value).split(" "));
        } else if (keyName.equals("uomLabels")) {
            this.uomLabels = Arrays.asList(((String) value).split(" "));
        }
    }
}
