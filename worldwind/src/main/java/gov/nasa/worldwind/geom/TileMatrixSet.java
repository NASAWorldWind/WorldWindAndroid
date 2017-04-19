/*
 * Copyright (c) 2017 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.geom;

import java.util.ArrayList;
import java.util.List;

import gov.nasa.worldwind.util.Logger;

public class TileMatrixSet {

    public Sector sector = new Sector();

    protected TileMatrix[] entries = new TileMatrix[0];

    public TileMatrixSet() {
    }

    public TileMatrixSet(Sector sector, List<TileMatrix> tileMatrixList) {
        if (sector == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "TileMatrixSet", "constructor", "missingSector"));
        }

        if (tileMatrixList == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "TileMatrixSet", "constructor", "missingList"));
        }

        this.sector.set(sector);
        this.entries = tileMatrixList.toArray(new TileMatrix[tileMatrixList.size()]);
    }

    public static TileMatrixSet fromTilePyramid(Sector sector, int matrixWidth, int matrixHeight, int tileWidth, int tileHeight, int numLevels) {
        if (sector == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "TileMatrixSet", "fromTilePyramid", "missingSector"));
        }

        ArrayList<TileMatrix> tileMatrices = new ArrayList<>();

        for (int idx = 0; idx < numLevels; idx++) {
            TileMatrix matrix = new TileMatrix();
            matrix.sector.set(sector);
            matrix.ordinal = idx;
            matrix.matrixWidth = matrixWidth;
            matrix.matrixHeight = matrixHeight;
            matrix.tileWidth = tileWidth;
            matrix.tileHeight = tileHeight;
            tileMatrices.add(matrix);

            matrixWidth *= 2;
            matrixHeight *= 2;
        }

        return new TileMatrixSet(sector, tileMatrices);
    }

    /**
     * Returns the number of matrices in this tile matrix set.
     *
     * @return the number of matrices
     */
    public int count() {
        return this.entries.length;
    }

    /**
     * Returns the matrix for a specified ordinal.
     *
     * @param index the ordinal of the desired matrix
     *
     * @return the requested matrix, or null if the matrix does not exist
     */
    public TileMatrix matrix(int index) {
        if (index < 0 || index >= this.entries.length) {
            return null;
        } else {
            return this.entries[index];
        }
    }

    public int indexOfMatrixNearest(double degreesPerPixel) {
        int nearestIdx = -1;
        double nearestDelta2 = Double.POSITIVE_INFINITY;

        for (int idx = 0, len = this.entries.length; idx < len; idx++) {
            double delta = (this.entries[idx].degreesPerPixel() - degreesPerPixel);
            double delta2 = delta * delta;

            if (nearestDelta2 > delta2) {
                nearestDelta2 = delta2;
                nearestIdx = idx;
            }
        }

        return nearestIdx;
    }
}
