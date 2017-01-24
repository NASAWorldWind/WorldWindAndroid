/*
 * Copyright (c) 2017 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.ogc.wmts;

import java.util.List;

import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.render.ImageSource;
import gov.nasa.worldwind.render.ImageTile;
import gov.nasa.worldwind.util.Level;
import gov.nasa.worldwind.util.Tile;
import gov.nasa.worldwind.util.TileFactory;

public class WmtsTileFactory implements TileFactory {

    public static String TILEMATRIX_TEMPLATE = "{TileMatrix}";

    public static String TILEROW_TEMPLATE = "{TileRow}";

    public static String TILECOL_TEMPLATE = "{TileCol}";

    protected int[] rowHeight;

    protected String template;

    protected List<String> tileMatrixIdentifiers;

    protected int imageSize;

    public WmtsTileFactory(String kvpServiceAddress, String layer, String format, String tileMatrixSet,
                           List<String> tileMatrixIdentifiers, int imageSize) {
        this.buildTemplate(kvpServiceAddress, layer, format, tileMatrixSet);
        this.tileMatrixIdentifiers = tileMatrixIdentifiers;
        this.initializeRowHeight();
        this.imageSize = imageSize;
    }

    public WmtsTileFactory(String template, List<String> tileMatrixIdentifiers, int imageSize) {
        this.template = template;
        this.tileMatrixIdentifiers = tileMatrixIdentifiers;
        this.initializeRowHeight();
        this.imageSize = imageSize;
    }

    @Override
    public Tile createTile(Sector sector, Level level, int row, int column) {

        ImageTile tile = new ImageTile(sector, level, row, column);

        String urlString = this.urlForTile(level.levelNumber, row, column);

        tile.setImageSource(ImageSource.fromUrl(urlString));

        return tile;
    }

    public String urlForTile(int level, int row, int column) {

        if (level < 0 || level >= this.rowHeight.length) {
            // TODO log message
            return null;
        }

        if (this.template == null || this.rowHeight == null) {
            // TODO log message
            return null;
        }

        if (level >= this.rowHeight.length) {
            // TODO log message
            return null;
        }

        if (level >= this.tileMatrixIdentifiers.size()) {
            // TODO log message
            return null;
        }

        // flip the row index
        int flipRow = this.rowHeight[level] - row - 1;

        String url = this.template.replace(TILEMATRIX_TEMPLATE, this.tileMatrixIdentifiers.get(level));
        url = url.replace(TILEROW_TEMPLATE, flipRow + "");
        url = url.replace(TILECOL_TEMPLATE, column + "");

        return url;
    }

    public int getNumberOfLevels() {
        return this.tileMatrixIdentifiers.size();
    }

    public int getImageSize() {
        return this.imageSize;
    }

    protected void initializeRowHeight() {
        this.rowHeight = new int[this.tileMatrixIdentifiers.size()];

        for (int i = 0; i < this.tileMatrixIdentifiers.size(); i++) {
            double modRow = Math.pow(2, i + 1);
            this.rowHeight[i] = ((int) modRow);
        }
    }

    protected void buildTemplate(String kvpServiceAddress, String layer, String format, String tileMatrixSet) {
        StringBuilder urlTemplate = new StringBuilder(kvpServiceAddress);

        int index = urlTemplate.indexOf("?");
        if (index < 0) { // if service address contains no query delimiter
            urlTemplate.append("?"); // add one
        } else if (index != urlTemplate.length() - 1) { // else if query delimiter not at end of string
            index = urlTemplate.lastIndexOf("&");
            if (index != urlTemplate.length() - 1) {
                urlTemplate.append("&"); // add a parameter delimiter
            }
        }

        urlTemplate.append("SERVICE=WMTS&");
        urlTemplate.append("REQUEST=GetTile&");
        urlTemplate.append("VERSION=1.0.0&");
        urlTemplate.append("LAYER=").append(layer).append("&");
        urlTemplate.append("STYLE=default&");
        urlTemplate.append("FORMAT=").append(format).append("&");
        urlTemplate.append("TILEMATRIXSET=").append(tileMatrixSet).append("&");
        urlTemplate.append("TILEMATRIX=").append(TILEMATRIX_TEMPLATE).append("&");
        urlTemplate.append("TILEROW=").append(TILEROW_TEMPLATE).append("&");
        urlTemplate.append("TILECOL=").append(TILECOL_TEMPLATE);

        this.template = urlTemplate.toString();
    }
}
