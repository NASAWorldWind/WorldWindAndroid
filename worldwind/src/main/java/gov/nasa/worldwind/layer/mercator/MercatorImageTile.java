package gov.nasa.worldwind.layer.mercator;

import android.graphics.Bitmap;

import java.util.Collection;

import gov.nasa.worldwind.render.ImageSource;
import gov.nasa.worldwind.render.ImageTile;
import gov.nasa.worldwind.util.Level;
import gov.nasa.worldwind.util.LevelSet;
import gov.nasa.worldwind.util.Logger;
import gov.nasa.worldwind.util.Tile;
import gov.nasa.worldwind.util.TileFactory;

class MercatorImageTile extends ImageTile implements ImageSource.Transformer {

    /**
     * Constructs a tile with a specified sector, level, row and column.
     *
     * @param sector the sector spanned by the tile
     * @param level  the tile's level in a {@link LevelSet}
     * @param row    the tile's row within the specified level
     * @param column the tile's column within the specified level
     */
    MercatorImageTile(MercatorSector sector, Level level, int row, int column) {
        super(sector, level, row, column);
    }

    /**
     * Creates all Mercator tiles for a specified level within a {@link LevelSet}.
     *
     * @param level       the level to create the tiles for
     * @param tileFactory the tile factory to use for creating tiles.
     * @param result      an pre-allocated Collection in which to store the results
     */
    static void assembleMercatorTilesForLevel(Level level, TileFactory tileFactory, Collection<Tile> result) {
        if (level == null) {
            throw new IllegalArgumentException(
                    Logger.logMessage(Logger.ERROR, "Tile", "assembleTilesForLevel", "missingLevel"));
        }

        if (tileFactory == null) {
            throw new IllegalArgumentException(
                    Logger.logMessage(Logger.ERROR, "Tile", "assembleTilesForLevel", "missingTileFactory"));
        }

        if (result == null) {
            throw new IllegalArgumentException(
                    Logger.logMessage(Logger.ERROR, "Tile", "assembleTilesForLevel", "missingResult"));
        }

        // NOTE LevelSet.sector is final Sector attribute and thus can not be cast to MercatorSector!
        MercatorSector sector = MercatorSector.fromSector(level.parent.sector);
        double dLat = level.tileDelta / 2;
        double dLon = level.tileDelta;

        int firstRow = Tile.computeRow(dLat, sector.minLatitude());
        int lastRow = Tile.computeLastRow(dLat, sector.maxLatitude());
        int firstCol = Tile.computeColumn(dLon, sector.minLongitude());
        int lastCol = Tile.computeLastColumn(dLon, sector.maxLongitude());

        double deltaLat = dLat / 90;
        double d1 = sector.minLatPercent() + deltaLat * firstRow;
        for (int row = firstRow; row <= lastRow; row++) {
            double d2 = d1 + deltaLat;
            double t1 = sector.minLongitude() + (firstCol * dLon);
            for (int col = firstCol; col <= lastCol; col++) {
                double t2;
                t2 = t1 + dLon;
                result.add(tileFactory.createTile(MercatorSector.fromDegrees(d1, d2, t1, t2), level, row, col));
                t1 = t2;
            }
            d1 = d2;
        }
    }

    /**
     * Returns the four children formed by subdividing this tile. This tile's sector is subdivided into four quadrants
     * as follows: Southwest; Southeast; Northwest; Northeast. A new tile is then constructed for each quadrant and
     * configured with the next level within this tile's LevelSet and its corresponding row and column within that
     * level. This returns null if this tile's level is the last level within its {@link LevelSet}.
     *
     * @param tileFactory the tile factory to use to create the children
     *
     * @return an array containing the four child tiles, or null if this tile's level is the last level
     */
    @Override
    public Tile[] subdivide(TileFactory tileFactory) {
        if (tileFactory == null) {
            throw new IllegalArgumentException(
                    Logger.logMessage(Logger.ERROR, "Tile", "subdivide", "missingTileFactory"));
        }

        Level childLevel = this.level.nextLevel();
        if (childLevel == null) {
            return null;
        }

        MercatorSector sector = (MercatorSector) this.sector;

        double d0 = sector.minLatPercent();
        double d2 = sector.maxLatPercent();
        double d1 = d0 + (d2 - d0) / 2.0;

        double t0 = sector.minLongitude();
        double t2 = sector.maxLongitude();
        double t1 = 0.5 * (t0 + t2);

        int northRow = 2 * this.row;
        int southRow = northRow + 1;
        int westCol = 2 * this.column;
        int eastCol = westCol + 1;

        Tile[] children = new Tile[4];
        children[0] = tileFactory.createTile(MercatorSector.fromDegrees(d0, d1, t0, t1), childLevel, northRow, westCol);
        children[1] = tileFactory.createTile(MercatorSector.fromDegrees(d0, d1, t1, t2), childLevel, northRow, eastCol);
        children[2] = tileFactory.createTile(MercatorSector.fromDegrees(d1, d2, t0, t1), childLevel, southRow, westCol);
        children[3] = tileFactory.createTile(MercatorSector.fromDegrees(d1, d2, t1, t2), childLevel, southRow, eastCol);

        return children;
    }

    @Override
    public Bitmap transform(Bitmap bitmap) {
        // Re-project mercator tile to equirectangular
        Bitmap trans = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), bitmap.getConfig());
        double miny = ((MercatorSector) sector).minLatPercent();
        double maxy = ((MercatorSector) sector).maxLatPercent();
        for (int y = 0; y < bitmap.getHeight(); y++) {
            double sy = 1.0 - y / (double) (bitmap.getHeight() - 1);
            double lat = sy * (sector.maxLatitude() - sector.minLatitude()) + sector.minLatitude();
            double dy = 1.0 - (MercatorSector.gudermannianInverse(lat) - miny) / (maxy - miny);
            dy = Math.max(0.0, Math.min(1.0, dy));
            int iy = (int) (dy * (bitmap.getHeight() - 1));
            for (int x = 0; x < bitmap.getWidth(); x++) {
                trans.setPixel(x, y, bitmap.getPixel(x, iy));
            }
        }
        return trans;
    }

}