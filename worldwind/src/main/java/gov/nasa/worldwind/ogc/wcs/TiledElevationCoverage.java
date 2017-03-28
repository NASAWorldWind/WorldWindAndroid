/*
 * Copyright (c) 2017 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.ogc.wcs;

import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.globe.AbstractElevationCoverage;
import gov.nasa.worldwind.util.Level;
import gov.nasa.worldwind.util.LevelSet;
import gov.nasa.worldwind.util.Logger;
import gov.nasa.worldwind.util.Tile;
import gov.nasa.worldwind.util.TileFactory;

public class TiledElevationCoverage extends AbstractElevationCoverage {

    protected TileFactory tileFactory;

    protected LevelSet levelSet = new LevelSet(); // empty level set

    public TiledElevationCoverage() {
    }

    public LevelSet getLevelSet() {
        return this.levelSet;
    }

    public void setLevelSet(LevelSet levelSet) {
        if (levelSet == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "TiledSurfaceImage", "setLevelSet", "missingLevelSet"));
        }

        this.levelSet = levelSet;
    }

    public TileFactory getTileFactory() {
        return this.tileFactory;
    }

    public void setTileFactory(TileFactory tileFactory) {
        this.tileFactory = tileFactory;
    }

    @Override
    public boolean hasCoverage(Sector sector) {
        return false;
    }

    @Override
    public float getHeight(double latitude, double longitude) {
        return 0; // TODO
    }

    @Override
    public float[] getHeight(Tile tile, float[] result) {
        Level level = this.levelSet.levelForResolution(tile.level.texelHeight);
        // TODO
        return new float[0];
    }

    @Override
    public float[] getHeightLimits(Tile tile, float[] result) {
        return new float[2]; // TODO
    }
}

// Internal. Returns elevations for a grid assuming pixel-is-area.
// ElevationModel.prototype.areaElevationsForGrid = function (sector, numLat, numLon, level, result) {
//     var minLat = sector.minLatitude,
//         maxLat = sector.maxLatitude,
//         minLon = sector.minLongitude,
//         maxLon = sector.maxLongitude,
//         deltaLat = sector.deltaLatitude() / (numLat > 1 ? numLat - 1 : 1),
//         deltaLon = sector.deltaLongitude() / (numLon > 1 ? numLon - 1 : 1),
//         lat, lon, s, t,
//         latIndex, lonIndex, resultIndex = 0;
//
//     for (latIndex = 0, lat = minLat; latIndex < numLat; latIndex += 1, lat += deltaLat) {
//         if (latIndex === numLat - 1) {
//             lat = maxLat; // explicitly set the last lat to the max latitude ensure alignment
//         }
//
//         for (lonIndex = 0, lon = minLon; lonIndex < numLon; lonIndex += 1, lon += deltaLon) {
//             if (lonIndex === numLon - 1) {
//                 lon = maxLon; // explicitly set the last lon to the max longitude ensure alignment
//             }
//
//             if (this.coverageSector.containsLocation(lat, lon)) { // ignore locations outside of the model
//                 s = (lon + 180) / 360;
//                 t = (lat + 90) / 180;
//                 this.areaElevationForCoord(s, t, level.levelNumber, result, resultIndex);
//             }
//
//             resultIndex++;
//         }
//     }
//
//     return level.texelSize; // TODO: return the actual achieved
// };

// Internal. Returns an elevation for a location assuming pixel-is-area.
// ElevationModel.prototype.areaElevationForCoord = function (s, t, levelNumber, result, resultIndex) {
//     var level, levelWidth, levelHeight,
//         tMin, tMax,
//         vMin, vMax,
//         u, v,
//         x0, x1, y0, y1,
//         xf, yf,
//         retrieveTiles,
//         pixels = new Float64Array(4);
//
//     for (var i = levelNumber; i >= 0; i--) {
//         level = this.levels.level(i);
//         levelWidth = Math.round(level.tileWidth * 360 / level.tileDelta.longitude);
//         levelHeight = Math.round(level.tileHeight * 180 / level.tileDelta.latitude);
//         tMin = 1 / (2 * levelHeight);
//         tMax = 1 - tMin;
//         vMin = 0;
//         vMax = levelHeight - 1;
//         u = levelWidth * WWMath.fract(s); // wrap the horizontal coordinate
//         v = levelHeight * WWMath.clamp(t, tMin, tMax); // clamp the vertical coordinate to the level edge
//         x0 = WWMath.mod(Math.floor(u - 0.5), levelWidth);
//         x1 = WWMath.mod((x0 + 1), levelWidth);
//         y0 = WWMath.clamp(Math.floor(v - 0.5), vMin, vMax);
//         y1 = WWMath.clamp(y0 + 1, vMin, vMax);
//         xf = WWMath.fract(u - 0.5);
//         yf = WWMath.fract(v - 0.5);
//         retrieveTiles = (i == levelNumber) || (i == 0);
//
//         if (this.lookupPixels(x0, x1, y0, y1, level, retrieveTiles, pixels)) {
//             result[resultIndex] = (1 - xf) * (1 - yf) * pixels[0] +
//                 xf * (1 - yf) * pixels[1] +
//                 (1 - xf) * yf * pixels[2] +
//                 xf * yf * pixels[3];
//             return;
//         }
//     }
// };

// // Internal. Bilinearly interpolates tile-image elevations.
// ElevationModel.prototype.lookupPixels = function (x0, x1, y0, y1, level, retrieveTiles, result) {
//     var levelNumber = level.levelNumber,
//         tileWidth = level.tileWidth,
//         tileHeight = level.tileHeight,
//         row0 = Math.floor(y0 / tileHeight),
//         row1 = Math.floor(y1 / tileHeight),
//         col0 = Math.floor(x0 / tileWidth),
//         col1 = Math.floor(x1 / tileWidth),
//         r0c0, r0c1, r1c0, r1c1;
//
//     if (row0 == row1 && row0 == this.cachedRow && col0 == col1 && col0 == this.cachedCol) {
//         r0c0 = r0c1 = r1c0 = r1c1 = this.cachedImage; // use results from previous lookup
//     } else if (row0 == row1 && col0 == col1) {
//         r0c0 = this.lookupImage(levelNumber, row0, col0, retrieveTiles); // only need to lookup one image
//         r0c1 = r1c0 = r1c1 = r0c0; // re-use the single image
//         this.cachedRow = row0;
//         this.cachedCol = col0;
//         this.cachedImage = r0c0; // note the results for subsequent lookups
//     } else {
//         r0c0 = this.lookupImage(levelNumber, row0, col0, retrieveTiles);
//         r0c1 = this.lookupImage(levelNumber, row0, col1, retrieveTiles);
//         r1c0 = this.lookupImage(levelNumber, row1, col0, retrieveTiles);
//         r1c1 = this.lookupImage(levelNumber, row1, col1, retrieveTiles);
//     }
//
//     if (r0c0 && r0c1 && r1c0 && r1c1) {
//         result[0] = r0c0.pixel(x0 % tileWidth, y0 % tileHeight);
//         result[1] = r0c1.pixel(x1 % tileWidth, y0 % tileHeight);
//         result[2] = r1c0.pixel(x0 % tileWidth, y1 % tileHeight);
//         result[3] = r1c1.pixel(x1 % tileWidth, y1 % tileHeight);
//         return true;
//     }
//
//     return false;
// };

// Internal. Intentionally not documented.
// ElevationModel.prototype.lookupImage = function (levelNumber, row, column, retrieveTiles) {
//     var tile = this.tileForLevel(levelNumber, row, column),
//         image = tile.image();
//
//     // If the tile's elevations have expired, cause it to be re-retrieved. Note that the current,
//     // expired elevations are still used until the updated ones arrive.
//     if (image == null && retrieveTiles) {
//         this.retrieveTileImage(tile);
//     }
//
//     return image;
// };

// Intentionally not documented.
// ElevationModel.prototype.tileForLevel = function (levelNumber, row, column) {
//     var tileKey = levelNumber + "." + row + "." + column,
//         tile = this.tileCache.entryForKey(tileKey);
//
//     if (tile) {
//         return tile;
//     }
//
//     var level = this.levels.level(levelNumber),
//         sector = Tile.computeSector(level, row, column);
//
//     tile = this.createTile(sector, level, row, column);
//     this.tileCache.putEntry(tileKey, tile, tile.size());
//
//     return tile;
// };

// // Intentionally not documented.
// ElevationModel.prototype.loadElevationImage = function (tile, xhr) {
//     var elevationImage = new ElevationImage(tile.imagePath, tile.sector, tile.tileWidth, tile.tileHeight);
//
//     if (this.retrievalImageFormat == "application/bil16") {
//         elevationImage.imageData = new Int16Array(xhr.response);
//         elevationImage.size = elevationImage.imageData.length * 2;
//     } else if (this.retrievalImageFormat == "application/bil32") {
//         elevationImage.imageData = new Float32Array(xhr.response);
//         elevationImage.size = elevationImage.imageData.length * 4;
//     }
//
//     if (elevationImage.imageData) {
//         elevationImage.findMinAndMaxElevation();
//         this.imageCache.putEntry(tile.imagePath, elevationImage, elevationImage.size);
//         this.timestamp = Date.now();
//     }
// };
