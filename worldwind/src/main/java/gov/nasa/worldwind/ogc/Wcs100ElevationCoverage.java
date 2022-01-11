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
 * Generates elevations from OGC Web Coverage Service (WCS) version 1.0.0.
 * <br>
 * Wcs100ElevationCoverage requires the WCS service address, coverage name, and coverage bounding sector. Get Coverage
 * requests generated for retrieving data use the WCS version 1.0.0 protocol and are limited to the "image/tiff" format
 * and the EPSG:4326 coordinate system. Wcs100ElevationCoverage does not perform version negotation and assumes the
 * service supports the format and coordinate system parameters detailed here.
 */
public class Wcs100ElevationCoverage extends TiledElevationCoverage {

    /**
     * Constructs a Web Coverage Service (WCS) elevation coverage with specified WCS configuration values.
     *
     * @param sector         the coverage's geographic bounding sector
     * @param numLevels      the number of levels of elevations to generate, beginning with 2-by-4 geographic grid of
     *                       90-degree tiles containing 256x256 elevation pixels
     * @param serviceAddress the WCS service address
     * @param coverage       the WCS coverage name
     *
     * @throws IllegalArgumentException If any argument is null or if the number of levels is less than 0
     */
    public Wcs100ElevationCoverage(Sector sector, int numLevels, String serviceAddress, String coverage) {
        if (sector == null) {
            throw new IllegalArgumentException(
                Logger.makeMessage("Wcs100ElevationCoverage", "constructor", "missingSector"));
        }

        if (numLevels < 0) {
            throw new IllegalArgumentException(
                Logger.makeMessage("Wcs100ElevationCoverage", "constructor", "invalidNumLevels"));
        }

        if (serviceAddress == null) {
            throw new IllegalArgumentException(
                Logger.makeMessage("Wcs100ElevationCoverage", "constructor", "missingServiceAddress"));
        }

        if (coverage == null) {
            throw new IllegalArgumentException(
                Logger.makeMessage("Wcs100ElevationCoverage", "constructor", "missingCoverage"));
        }

        int matrixWidth = sector.isFullSphere() ? 2 : 1;
        int matrixHeight = 1;
        int tileWidth = 256;
        int tileHeight = 256;

        this.setTileMatrixSet(TileMatrixSet.fromTilePyramid(sector, matrixWidth, matrixHeight, tileWidth, tileHeight, numLevels));
        this.setTileFactory(new Wcs100TileFactory(serviceAddress, coverage));
    }
}
