/*
 * Copyright (c) 2016 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwindx.experimental;

import java.io.IOException;

import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.util.WWUtil;
import gov.nasa.worldwindx.R;

public class SkyProgram extends AtmosphereProgram {

    public SkyProgram(DrawContext dc) throws IOException {
        super(dc, WWUtil.readResourceAsText(dc.getResources(), R.raw.gov_nasa_worldwind_skyprogram_vert),
            WWUtil.readResourceAsText(dc.getResources(), R.raw.gov_nasa_worldwind_atmosphereprogram_frag));
    }
}
