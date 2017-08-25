/*
 * Copyright (c) 2017 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.ogc;

import android.net.Uri;
import android.os.Handler;
import android.os.Looper;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.geom.TileMatrixSet;
import gov.nasa.worldwind.globe.TiledElevationCoverage;
import gov.nasa.worldwind.ogc.gml.GmlAbstractGeometry;
import gov.nasa.worldwind.ogc.gml.GmlRectifiedGrid;
import gov.nasa.worldwind.ogc.ows.OwsExceptionReport;
import gov.nasa.worldwind.ogc.ows.OwsXmlParser;
import gov.nasa.worldwind.ogc.wcs.Wcs201CoverageDescription;
import gov.nasa.worldwind.ogc.wcs.Wcs201CoverageDescriptions;
import gov.nasa.worldwind.ogc.wcs.WcsXmlParser;
import gov.nasa.worldwind.util.Logger;
import gov.nasa.worldwind.util.WWUtil;

/**
 * Generates elevations from OGC Web Coverage Service (WCS) version 2.0.1.
 * <p/>
 * Wcs201ElevationCoverage requires the WCS service address, coverage name, and coverage bounding sector. Get Coverage
 * requests generated for retrieving data use the WCS version 2.0.1 protocol and are limited to the "image/tiff" format
 * and the EPSG:4326 coordinate system. Wcs201ElevationCoverage does not perform version negotiation and assumes the
 * service supports the format and coordinate system parameters detailed here. The subset CRS is configured as EPSG:4326
 * and the axis labels are set as "Lat" and "Long". The scaling axis labels are set as:
 * <p/>
 * http://www.opengis.net/def/axis/OGC/1/i and http://www.opengis.net/def/axis/OGC/1/j
 */
public class Wcs201ElevationCoverage extends TiledElevationCoverage {

    protected Handler handler = new Handler(Looper.getMainLooper());

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
    public Wcs201ElevationCoverage(Sector sector, int numLevels, String serviceAddress, String coverage) {
        if (sector == null) {
            throw new IllegalArgumentException(
                Logger.makeMessage("Wcs201ElevationCoverage", "constructor", "missingSector"));
        }

        if (numLevels < 0) {
            throw new IllegalArgumentException(
                Logger.makeMessage("Wcs201ElevationCoverage", "constructor", "invalidNumLevels"));
        }

        if (serviceAddress == null) {
            throw new IllegalArgumentException(
                Logger.makeMessage("Wcs201ElevationCoverage", "constructor", "missingServiceAddress"));
        }

        if (coverage == null) {
            throw new IllegalArgumentException(
                Logger.makeMessage("Wcs201ElevationCoverage", "constructor", "missingCoverage"));
        }

        if (numLevels < 0) {
            throw new IllegalArgumentException(
                Logger.makeMessage("Wcs201ElevationCoverage", "constructor", "The number of levels must be greater than 0"));
        }

        int matrixWidth = sector.isFullSphere() ? 2 : 1;
        int matrixHeight = 1;
        int tileWidth = 256;
        int tileHeight = 256;

        this.setTileMatrixSet(TileMatrixSet.fromTilePyramid(sector, matrixWidth, matrixHeight, tileWidth, tileHeight, numLevels));
        this.setTileFactory(new Wcs201TileFactory(serviceAddress, coverage));
    }

    /**
     * Attempts to construct a Web Coverage Service (WCS) elevation coverage with the provided service address and
     * coverage id. This constructor initiates an asynchronous request for the DescribeCoverage document and then uses
     * the information provided to determine a suitable Sector and level count. If the coverage id doesn't match the
     * available coverages or there is another error, no data will be provided and the error will be logged.
     *
     * @param serviceAddress the WCS service address
     * @param coverage       the WCS coverage name
     *
     * @throws IllegalArgumentException If any argument is null
     */
    public Wcs201ElevationCoverage(String serviceAddress, String coverage) {
        if (serviceAddress == null) {
            throw new IllegalArgumentException(
                Logger.makeMessage("Wcs201ElevationCoverage", "constructor", "missingServiceAddress"));
        }

        if (coverage == null) {
            throw new IllegalArgumentException(
                Logger.makeMessage("Wcs201ElevationCoverage", "constructor", "missingCoverage"));
        }

        // Fetch the DescribeCoverage document and determine the bounding box and number of levels
        final String finalServiceAddress = serviceAddress;
        final String finalCoverageId = coverage;
        WorldWind.taskService().execute(new Runnable() {
            @Override
            public void run() {
                try {
                    initAsync(finalServiceAddress, finalCoverageId);
                } catch (Throwable logged) {
                    Logger.logMessage(Logger.ERROR, "Wcs201ElevationCoverage", "constructor",
                        "Exception initializing WCS coverage serviceAddress:" + finalServiceAddress + " coverage:" + finalCoverageId, logged);
                }
            }
        });
    }

    protected void initAsync(String serviceAddress, String coverage) throws Exception {
        Wcs201CoverageDescriptions coverageDescriptions = this.describeCoverage(serviceAddress, coverage);
        Wcs201CoverageDescription coverageDescription = coverageDescriptions.getCoverageDescription(coverage);
        if (coverageDescription == null) {
            throw new Exception(
                Logger.makeMessage("Wcs201ElevationCoverage", "initAsync", "WCS coverage is undefined: " + coverage));
        }

        final TileFactory factory = new Wcs201TileFactory(serviceAddress, coverage);
        final TileMatrixSet matrixSet = this.tileMatrixSetFromCoverageDescription(coverageDescription);

        handler.post(new Runnable() {
            @Override
            public void run() {
                setTileFactory(factory);
                setTileMatrixSet(matrixSet);
                WorldWind.requestRedraw();
            }
        });
    }

    protected TileMatrixSet tileMatrixSetFromCoverageDescription(Wcs201CoverageDescription coverageDescription) throws Exception {
        String srsName = coverageDescription.getBoundedBy().getEnvelope().getSrsName();
        if (srsName == null || !srsName.contains("4326")) {
            throw new Exception(
                Logger.makeMessage("Wcs201ElevationCoverage", "tileMatrixSetFromCoverageDescription", "WCS Envelope SRS is incompatible: " + srsName));
        }

        double[] lowerCorner = coverageDescription.getBoundedBy().getEnvelope().getLowerCorner().getValues();
        double[] upperCorner = coverageDescription.getBoundedBy().getEnvelope().getUpperCorner().getValues();
        if (lowerCorner == null || upperCorner == null || lowerCorner.length != 2 || upperCorner.length != 2) {
            throw new Exception(
                Logger.makeMessage("Wcs201ElevationCoverage", "tileMatrixSetFromCoverageDescription", "WCS Envelope is invalid"));
        }

        // Determine the number of data points in the i and j directions
        GmlAbstractGeometry geometry = coverageDescription.getDomainSet().getGeometry();
        if (!(geometry instanceof GmlRectifiedGrid)) {
            throw new Exception(
                Logger.makeMessage("Wcs201ElevationCoverage", "tileMatrixSetFromCoverageDescription", "WCS domainSet Geometry is incompatible:" + geometry));
        }

        GmlRectifiedGrid grid = (GmlRectifiedGrid) geometry;
        int[] gridLow = grid.getLimits().getGridEnvelope().getLow().getValues();
        int[] gridHigh = grid.getLimits().getGridEnvelope().getHigh().getValues();
        if (gridLow == null || gridHigh == null || gridLow.length != 2 || gridHigh.length != 2) {
            throw new Exception(
                Logger.makeMessage("Wcs201ElevationCoverage", "tileMatrixSetFromCoverageDescription", "WCS GridEnvelope is invalid"));
        }

        Sector boundingSector = Sector.fromDegrees(lowerCorner[0], lowerCorner[1], upperCorner[0] - lowerCorner[0], upperCorner[1] - lowerCorner[1]);
        int tileWidth = 256;
        int tileHeight = 256;

        int gridHeight = gridHigh[1] - gridLow[1];
        double level = Math.log(gridHeight / tileHeight) / Math.log(2); // fractional level address
        int levelNumber = (int) Math.ceil(level); // ceiling captures the resolution

        if (levelNumber < 0) {
            levelNumber = 0; // need at least one level, even if it exceeds the desired resolution
        }

        int numLevels = levelNumber + 1; // convert level number to level count

        return TileMatrixSet.fromTilePyramid(boundingSector, boundingSector.isFullSphere() ? 2 : 1, 1, tileWidth, tileHeight, numLevels);
    }

    protected Wcs201CoverageDescriptions describeCoverage(String serviceAddress, String coverageId) throws Exception {
        InputStream inputStream = null;
        Object responseXml = null;
        try {
            // Build the appropriate request Uri given the provided service address
            Uri serviceUri = Uri.parse(serviceAddress).buildUpon()
                .appendQueryParameter("VERSION", "2.0.1")
                .appendQueryParameter("SERVICE", "WCS")
                .appendQueryParameter("REQUEST", "DescribeCoverage")
                .appendQueryParameter("COVERAGEID", coverageId)
                .build();

            // Open the connection as an input stream
            URLConnection conn = new URL(serviceUri.toString()).openConnection();
            conn.setConnectTimeout(3000);
            conn.setReadTimeout(30000);

            // Throw an exception when the service responded with an error
            OwsExceptionReport exceptionReport = OwsXmlParser.parseErrorStream(conn);
            if (exceptionReport != null) {
                throw new OgcException(exceptionReport);
            }

            // Parse and read the input stream
            inputStream = new BufferedInputStream(conn.getInputStream());
            responseXml = WcsXmlParser.parse(inputStream);
            if (responseXml instanceof OwsExceptionReport) {
                throw new OgcException((OwsExceptionReport) responseXml);
            } else if (!(responseXml instanceof Wcs201CoverageDescriptions)) {
                throw new Exception(
                    Logger.makeMessage("Wcs201ElevationCoverage", "describeCoverage", "Response is not a WCS DescribeCoverage document"));
            }
        } finally {
            WWUtil.closeSilently(inputStream);
        }

        return (Wcs201CoverageDescriptions) responseXml;
    }
}
