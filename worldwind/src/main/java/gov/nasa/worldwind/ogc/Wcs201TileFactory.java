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

public class Wcs201TileFactory implements TiledElevationCoverage.TileFactory {

    /**
     * The WCS service address use to build Get Coverage URLs.
     */
    protected String serviceAddress;

    /**
     * The coverage id of the desired WCS coverage.
     */
    protected String coverageId;

    /**
     * Constructs a WCS Get Coverage URL builder with the specified WCS service address and coverage. The generated URL
     * will be pursuant to version 2.0.1 WCS specification and use image/tiff as the format and EPSG:4326 as the
     * coordinate reference system. The requests will also include a GeoServer specific parameter 'overviewpolicy' which
     * expedites requests server side. This parameter should be ignored when server implementations other than GeoServer
     * are used.
     *
     * @param serviceAddress the WCS service address
     * @param coverageId     the WCS coverage name
     *
     * @throws IllegalArgumentException If any of the parameters are null
     */
    public Wcs201TileFactory(String serviceAddress, String coverageId) {
        if (serviceAddress == null) {
            throw new IllegalArgumentException(
                Logger.makeMessage("Wcs201TileFactory", "constructor", "missingServiceAddress"));
        }

        if (coverageId == null) {
            throw new IllegalArgumentException(
                Logger.makeMessage("Wcs201TileFactory", "constructor", "missingCoverage"));
        }

        this.serviceAddress = serviceAddress;
        this.coverageId = coverageId;
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
                Logger.logMessage(Logger.ERROR, "Wcs201TileFactory", "setServiceAddress", "missingServiceAddress"));
        }

        this.serviceAddress = serviceAddress;
    }

    /**
     * Indicates the coverage name used to build Get Coverage URLs.
     *
     * @return the coverage name
     */
    public String getCoverageId() {
        return this.coverageId;
    }

    /**
     * Sets the coverage name used to build Get Coverage URLs.
     *
     * @param coverageId the coverage id
     *
     * @throws IllegalArgumentException If the coverage name is null
     */
    public void setCoverageId(String coverageId) {
        if (coverageId == null) {
            throw new IllegalArgumentException(
                Logger.makeMessage("Wcs201TileFactory", "setCoverage", "missingCoverage"));
        }

        this.coverageId = coverageId;
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

        url.append("&VERSION=2.0.1");
        url.append("&REQUEST=GetCoverage");
        url.append("&COVERAGEID=").append(this.coverageId);
        url.append("&FORMAT=image/tiff");
        url.append("&SUBSET=Lat(");
        url.append(sector.minLatitude()).append(",").append(sector.maxLatitude()).append(")");
        url.append("&SUBSET=Long(");
        url.append(sector.minLongitude()).append(",").append(sector.maxLongitude()).append(")");
        url.append("&SCALESIZE=");
        url.append("http://www.opengis.net/def/axis/OGC/1/i(").append(tileMatrix.tileWidth).append("),");
        url.append("http://www.opengis.net/def/axis/OGC/1/j(").append(tileMatrix.tileHeight).append(")");
        url.append("&OVERVIEWPOLICY=NEAREST");

        return url.toString();
    }
}
