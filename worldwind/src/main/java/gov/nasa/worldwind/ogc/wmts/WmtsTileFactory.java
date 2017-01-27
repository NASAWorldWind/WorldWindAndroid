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
import gov.nasa.worldwind.util.Logger;
import gov.nasa.worldwind.util.Tile;
import gov.nasa.worldwind.util.TileFactory;

public class WmtsTileFactory implements TileFactory {

    public static String TILEMATRIX_TEMPLATE = "{TileMatrix}";

    public static String TILEROW_TEMPLATE = "{TileRow}";

    public static String TILECOL_TEMPLATE = "{TileCol}";

    protected String template;

    protected List<String> tileMatrixIdentifiers;

    public WmtsTileFactory() {

    }

    public WmtsTileFactory(String template, List<String> tileMatrixIdentifiers) {
        this.template = template;
        this.tileMatrixIdentifiers = tileMatrixIdentifiers;
    }

    @Override
    public Tile createTile(Sector sector, Level level, int row, int column) {
        ImageTile tile = new ImageTile(sector, level, row, column);

        String urlString = this.urlForTile(level.levelNumber, row, column);
        if (urlString != null) {
            tile.setImageSource(ImageSource.fromUrl(urlString));
        }

        return tile;
    }

    public String urlForTile(int level, int row, int column) {
        if (this.template == null || this.tileMatrixIdentifiers == null) {
            Logger.logMessage(Logger.WARN, "WmtsTileFactory", "urlForTile", "null template, rowHeight, or tileMatrixIdentifiers");
            return null;
        }

        if (level >= this.tileMatrixIdentifiers.size()) {
            Logger.logMessage(Logger.WARN, "WmtsTileFactory", "urlForTile", "invalid level for tileMatrixIdentifiers: " + level);
            return null;
        }

        // flip the row index
        int rowHeight = 2 << level;
        int flipRow = rowHeight - row - 1;

        String url = this.template.replace(TILEMATRIX_TEMPLATE, this.tileMatrixIdentifiers.get(level));
        url = url.replace(TILEROW_TEMPLATE, flipRow + "");
        url = url.replace(TILECOL_TEMPLATE, column + "");

        return url;
    }

    public String getTemplate() {
        return this.template;
    }

    public void setTemplate(String template) {
        this.template = template;
    }

    public List<String> getTileMatrixIdentifiers() {
        return this.tileMatrixIdentifiers;
    }

    public void setTileMatrixIdentifiers(List<String> tileMatrixIdentifiers) {
        this.tileMatrixIdentifiers = tileMatrixIdentifiers;
    }
}
