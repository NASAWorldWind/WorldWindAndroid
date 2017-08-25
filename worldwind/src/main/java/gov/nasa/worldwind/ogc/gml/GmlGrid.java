/*
 * Copyright (c) 2017 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.ogc.gml;

import java.util.ArrayList;
import java.util.List;

public class GmlGrid extends GmlAbstractGeometry {

    protected GmlGridLimits limits;

    protected List<String> axisNames = new ArrayList<>();

    protected String dimension;

    public GmlGrid() {
    }

    public GmlGridLimits getLimits() {
        return limits;
    }

    public List<String> getAxisNames() {
        return axisNames;
    }

    public String getDimension() {
        return dimension;
    }

    @Override
    protected void parseField(String keyName, Object value) {
        super.parseField(keyName, value);

        switch (keyName) {
            case "limits":
                limits = (GmlGridLimits) value;
                break;
            case "axisName":
                axisNames.add((String) value);
                break;
            case "dimension":
                dimension = (String) value;
                break;
        }
    }
}
