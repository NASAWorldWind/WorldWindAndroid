/*
 * Copyright (c) 2017 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.ogc.gml;

import java.util.Arrays;
import java.util.List;

public class GmlDirectPosition extends GmlDoubleList {

    protected String srsName;

    protected String srsDimension;

    protected List<String> axisLabels;

    protected List<String> uomLabels;

    public GmlDirectPosition() {
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

        if (keyName.equals("srsName")) {
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
