/*
 * Copyright (c) 2017 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.ogc;

import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.geom.TileMatrixSet;
import gov.nasa.worldwind.globe.TiledElevationCoverage;
import gov.nasa.worldwind.util.Logger;

/**
 * Generates terrain from OGC Web Coverage Service (WCS) version 1.0.0.
 * <p/>
 * Wcs100ElevationCoverage requires the service address, coverage name, and bounding sector of the coverage. Get
 * Coverage requests generated for retrieving data will use the WCS version 1.0.0 specification and be limited to the
 * "image/tiff" format and EPSG:4326 coordinate system. Wcs100ElevationCoverage does not conduct and version, coordinate
 * system, or version coordination and assumes the server will support parameters detailed here.
 */
public class Wcs100ElevationCoverage extends TiledElevationCoverage {

    /**
     * Constructs a WCS Elevation Coverage given the provided sector, number of levels, service address, and coverage
     * name.
     *
     * @param sector         the coverage bounding sector
     * @param numLevels      the number of levels
     * @param serviceAddress the WCS service address
     * @param coverage       the coverage name
     *
     * @throws IllegalArgumentException If any argument is null or the number of levels is less than 0.
     */
    public Wcs100ElevationCoverage(Sector sector, int numLevels, String serviceAddress, String coverage) {
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

        if (numLevels < 0) {
            throw new IllegalArgumentException(
                Logger.makeMessage("Wcs100ElevationCoverage", "constructor", "The number of levels must be greater than 0"));
        }

        int matrixWidth = sector.isFullSphere() ? 2 : 1;
        int matrixHeight = 1;
        int tileWidth = 256;
        int tileHeight = 256;

        this.setTileMatrixSet(new TileMatrixSet(sector, matrixWidth, matrixHeight, tileWidth, tileHeight, numLevels));
        this.setTileFactory(new Wcs100TileFactory(serviceAddress, coverage));
    }
}
