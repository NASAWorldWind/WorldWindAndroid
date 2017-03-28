/*
 * Copyright (c) 2017 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.globe;

import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.util.Level;
import gov.nasa.worldwind.util.Tile;

public class ElevationTile extends Tile {

    protected String coverageSource;

    public ElevationTile(Sector sector, Level level, int row, int column) {
        super(sector, level, row, column);
    }

    public String getCoverageSource() {
        return coverageSource;
    }

    public void setCoverageSource(String coverageSource) {
        this.coverageSource = coverageSource;
    }
}
