/*
 * Copyright (c) 2017 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.ogc;

import gov.nasa.worldwind.globe.TiledElevationCoverage;
import gov.nasa.worldwind.util.LevelSet;
import gov.nasa.worldwind.util.LevelSetConfig;
import gov.nasa.worldwind.util.Logger;

/**
 * A simple interface for providing WCS elevation values from a version 1.0.0 WCS. This class assumes the server has
 * been configured to provide "image/tiff" data in single band two's singed integer complement 2 byte values in meters.
 */
public class Wcs100ElevationCoverage extends TiledElevationCoverage {

    public Wcs100ElevationCoverage(String serviceAddress, String coverage) {
        if (serviceAddress == null) {
            throw new IllegalArgumentException(
                Logger.makeMessage("Wcs100ElevationCoverage", "constructor", "missingServiceAddress"));
        }

        if (coverage == null) {
            throw new IllegalArgumentException(
                Logger.makeMessage("Wcs100ElevationCoverage", "constructor", "The coverage is null"));
        }

        LevelSetConfig levelSetConfig = new LevelSetConfig();
        levelSetConfig.numLevels = 15;
        this.setLevelSet(new LevelSet(levelSetConfig));

        this.setTileFactory(new Wcs100TileFactory(serviceAddress, coverage));
    }
}
