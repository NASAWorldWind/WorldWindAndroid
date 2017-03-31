/*
 * Copyright (c) 2017 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.ogc;

import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.render.ImageSource;
import gov.nasa.worldwind.render.ImageTile;
import gov.nasa.worldwind.util.Level;
import gov.nasa.worldwind.util.Logger;
import gov.nasa.worldwind.util.Tile;
import gov.nasa.worldwind.util.TileFactory;

/**
 * An implementation of {@link TileFactory} which supports simple {@link Tile} generation from a WCS version 1.0.0
 * server. This class assumes the server has been configured to provide "image/tiff" data in single band two's singed
 * integer complement 2 byte values in meters with EPSG:4326 as the coordinate reference system.
 */
public class Wcs100TileFactory implements TileFactory {

    protected String serviceAddress;

    protected String coverage;

    public Wcs100TileFactory(String serviceAddress, String coverage) {
        if (serviceAddress == null) {
            throw new IllegalArgumentException(
                Logger.makeMessage("Wcs100TileFactory", "constructor", "missingServiceAddress"));
        }

        if (coverage == null) {
            throw new IllegalArgumentException(
                Logger.makeMessage("Wcs100TileFactory", "constructor", "The coverage is null"));
        }

        this.serviceAddress = serviceAddress;
        this.coverage = coverage;
    }

    @Override
    public Tile createTile(Sector sector, Level level, int row, int column) {
        ImageTile tile = new ImageTile(sector, level, row, column);

        String urlString = this.urlForTile(sector, level);
        if (urlString != null) {
            tile.setImageSource(ImageSource.fromUrl(urlString));
        }

        return tile;
    }

    protected String urlForTile(Sector sector, Level level) {
        StringBuilder url = new StringBuilder(this.serviceAddress);

        int index = url.indexOf("?");
        if (index < 0) { // if service address contains no query delimiter
            url.append("?"); // add one
        } else if (index != url.length() - 1) { // else if query delimiter not at end of string
            index = url.lastIndexOf("&");
            if (index != url.length() - 1) {
                url.append("&"); // add a parameter delimiter
            }
        }

        url.append("SERVICE=WCS&VERSION=1.0.0&REQUEST=GetCoverage&COVERAGE=").append(this.coverage).append("&");
        url.append("CRS=EPSG:4326&FORMAT=image/tiff&");
        url.append("WIDTH=").append(level.tileWidth).append("&");
        url.append("HEIGHT=").append(level.tileHeight).append("&");
        url.append("BBOX=").append(sector.minLongitude()).append(",").append(sector.minLatitude()).append(",");
        url.append(sector.maxLongitude()).append(",").append(sector.maxLatitude());

        return url.toString();
    }
}
