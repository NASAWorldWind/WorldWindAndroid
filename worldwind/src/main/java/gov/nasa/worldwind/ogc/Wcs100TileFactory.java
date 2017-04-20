/*
 * Copyright (c) 2017 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.ogc;

import java.util.Locale;

import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.geom.TileMatrix;
import gov.nasa.worldwind.globe.TiledElevationCoverage;
import gov.nasa.worldwind.render.ImageSource;
import gov.nasa.worldwind.util.Logger;

/**
 * Factory for constructing WCS version 1.0.0 URLs associated with WCS Get Coverage requests.
 */
public class Wcs100TileFactory implements TiledElevationCoverage.TileFactory {

    /**
     * The WCS service address use to build Get Coverage URLs.
     */
    protected String serviceAddress;

    /**
     * The coverage name of the desired WCS coverage.
     */
    protected String coverage;

    /**
     * Constructs a WCS Get Coverage URL builder with the specified WCS service address and coverage. The generated URL
     * will be pursuant to version 1.0.0 WCS specification and use image/tiff as the format and EPSG:4326 as the
     * coordinate reference system.
     *
     * @param serviceAddress the WCS service address
     * @param coverage       the WCS coverage name
     *
     * @throws IllegalArgumentException If any of the parameters are null
     */
    public Wcs100TileFactory(String serviceAddress, String coverage) {
        if (serviceAddress == null) {
            throw new IllegalArgumentException(
                Logger.makeMessage("Wcs100TileFactory", "constructor", "missingServiceAddress"));
        }

        if (coverage == null) {
            throw new IllegalArgumentException(
                Logger.makeMessage("Wcs100TileFactory", "constructor", "missingCoverage"));
        }

        this.serviceAddress = serviceAddress;
        this.coverage = coverage;
    }

    /**
     * Indicates the WCS service address used to build Get Coverage URLs.
     *
     * @return the WCS service address
     */
    public String getServiceAddress() {
        return this.serviceAddress;
    }

    /**
     * Sets the WCS service address used to build Get Coverage URLs.
     *
     * @param serviceAddress the WCS service address
     *
     * @throws IllegalArgumentException If the service address is null
     */
    public void setServiceAddress(String serviceAddress) {
        if (serviceAddress == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "Wcs100TileFactory", "setServiceAddress", "missingServiceAddress"));
        }

        this.serviceAddress = serviceAddress;
    }

    /**
     * Indicates the coverage name used to build Get Coverage URLs.
     *
     * @return the coverage name
     */
    public String getCoverage() {
        return this.coverage;
    }

    /**
     * Sets the coverage name used to build Get Coverage URLs.
     *
     * @param coverage the coverage name
     *
     * @throws IllegalArgumentException If the coverage name is null
     */
    public void setCoverage(String coverage) {
        if (coverage == null) {
            throw new IllegalArgumentException(
                Logger.makeMessage("Wcs100TileFactory", "setCoverage", "missingCoverage"));
        }

        this.coverage = coverage;
    }

    @Override
    public ImageSource createTileSource(TileMatrix tileMatrix, int row, int column) {
        String urlString = this.urlForTile(tileMatrix, row, column);
        return ImageSource.fromUrl(urlString);
    }

    protected String urlForTile(TileMatrix tileMatrix, int row, int col) {
        StringBuilder url = new StringBuilder(this.serviceAddress);
        Sector sector = tileMatrix.tileSector(row, col);

        int index = url.indexOf("?");
        if (index < 0) { // if service address contains no query delimiter
            url.append("?"); // add one
        } else if (index != url.length() - 1) { // else if query delimiter not at end of string
            index = url.lastIndexOf("&");
            if (index != url.length() - 1) {
                url.append("&"); // add a parameter delimiter
            }
        }

        index = this.serviceAddress.toUpperCase(Locale.US).indexOf("SERVICE=WCS");
        if (index < 0) {
            url.append("SERVICE=WCS");
        }

        url.append("&VERSION=1.0.0");
        url.append("&REQUEST=GetCoverage");
        url.append("&COVERAGE=").append(this.coverage);
        url.append("&CRS=EPSG:4326");
        url.append("&BBOX=")
            .append(sector.minLongitude()).append(",")
            .append(sector.minLatitude()).append(",")
            .append(sector.maxLongitude()).append(",")
            .append(sector.maxLatitude());
        url.append("&WIDTH=").append(tileMatrix.tileWidth);
        url.append("&HEIGHT=").append(tileMatrix.tileHeight);
        url.append("&FORMAT=image/tiff");

        return url.toString();
    }
}
