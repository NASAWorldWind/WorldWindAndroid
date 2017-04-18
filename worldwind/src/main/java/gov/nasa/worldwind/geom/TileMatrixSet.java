/*
 * Copyright (c) 2017 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.geom;

import gov.nasa.worldwind.util.Logger;

public class TileMatrixSet {

    public Sector sector = new Sector();

    protected TileMatrix[] entries = new TileMatrix[0];

    protected int size;

    public TileMatrixSet() {
    }

    public TileMatrixSet(Sector sector, int matrixWidth, int matrixHeight, int tileWidth, int tileHeight, int numLevels) {
        if (sector == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "TileMatrixSet", "constructor", "missingSector"));
        }

        this.sector.set(sector);
        this.entries = new TileMatrix[numLevels];
        this.size = numLevels;

        for (int idx = 0; idx < numLevels; idx++) {
            TileMatrix matrix = new TileMatrix();
            matrix.sector.set(sector);
            matrix.ordinal = idx;
            matrix.matrixWidth = matrixWidth;
            matrix.matrixHeight = matrixHeight;
            matrix.tileWidth = tileWidth;
            matrix.tileHeight = tileHeight;
            matrix.pixelSpanX = sector.deltaLongitude() / (matrixWidth * tileWidth);
            matrix.pixelSpanY = sector.deltaLatitude() / (matrixHeight * tileHeight);
            this.entries[idx] = matrix;

            matrixWidth *= 2;
            matrixHeight *= 2;
        }
    }

    /**
     * Returns the number of matrices in this tile matrix set.
     *
     * @return the number of matrices
     */
    public int count() {
        return this.size;
    }

    /**
     * Returns the matrix for a specified ordinal.
     *
     * @param index the ordinal of the desired matrix
     *
     * @return the requested matrix, or null if the matrix does not exist
     */
    public TileMatrix matrix(int index) {
        if (index < 0 || index >= this.size) {
            return null;
        } else {
            return this.entries[index];
        }
    }

    public int indexOfMatrixNearestPixelSpan(double pixelSpan) {
        int nearestIdx = -1;
        double nearestDelta2 = Double.POSITIVE_INFINITY;

        for (int idx = 0; idx < this.size; idx++) {
            double delta = (this.entries[idx].pixelSpanX - pixelSpan);
            double delta2 = delta * delta;

            if (nearestDelta2 > delta2) {
                nearestDelta2 = delta2;
                nearestIdx = idx;
            }
        }

        return nearestIdx;
    }
}
