/*
 * Copyright (c) 2017 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.ogc;

import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.globe.TiledElevationCoverage;
import gov.nasa.worldwind.util.LevelSet;
import gov.nasa.worldwind.util.LevelSetConfig;
import gov.nasa.worldwind.util.Logger;

/**
 * Generates terrain from OGC Web Coverage Service (WCS) version 1.0.0.
 * <p/>
 * Wcs100ElevationCoverage requires the service address, coverage name, and bounding sector of the coverage. Get
 * Coverage requests generated for retrieving data will use the WCS version 1.0.0 specification and be limited to the
 * "image/tiff" format and EPSG:4326 coordinate system. Wcs100ElevationCoverage does not conduct and version, coordinate
 * system, or version coordination and assumes the server will support parameters detailed here. The default LevelSet
 * level limit is 15.
 */
public class Wcs100ElevationCoverage extends TiledElevationCoverage {

    /**
     * Constructs a WCS Elevation Coverage given the provided service address, coverage name, and sector.
     *
     * @param serviceAddress the WCS service address
     * @param coverage       the coverage name
     * @param sector         the coverage bounding sector
     *
     * @throws IllegalArgumentException If any argument is null
     */
    public Wcs100ElevationCoverage(String serviceAddress, String coverage, Sector sector) {
        if (serviceAddress == null) {
            throw new IllegalArgumentException(
                Logger.makeMessage("Wcs100ElevationCoverage", "constructor", "missingServiceAddress"));
        }

        if (coverage == null) {
            throw new IllegalArgumentException(
                Logger.makeMessage("Wcs100ElevationCoverage", "constructor", "The coverage is null"));
        }

        if (sector == null) {
            throw new IllegalArgumentException(
                Logger.makeMessage("Wcs100ElevationCoverage", "constructor", "The sector is null"));
        }

        LevelSetConfig levelSetConfig = new LevelSetConfig(sector, 90.0, 15, 256, 256);
        this.setLevelSet(new LevelSet(levelSetConfig));

        this.setTileFactory(new Wcs100TileFactory(serviceAddress, coverage));
    }
}
