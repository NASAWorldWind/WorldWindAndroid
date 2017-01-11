/*
 * Copyright (c) 2017 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.ogc.gpkg;

import java.util.Arrays;

public class GpkgTileUserMetrics extends GpkgEntry {

    protected int[] zoomLevels;

    public GpkgTileUserMetrics() {
    }

    public int[] getZoomLevels() {
        return zoomLevels;
    }

    public void setZoomLevels(int[] zoomLevels) {
        this.zoomLevels = zoomLevels;
        Arrays.sort(this.zoomLevels);
    }

    public int getMinZoomLevel() {
        int len = this.zoomLevels.length;
        return (len == 0) ? -1 : this.zoomLevels[0];
    }

    public int getMaxZoomLevel() {
        int len = this.zoomLevels.length;
        return (len == 0) ? -1 : this.zoomLevels[len - 1];
    }

    public boolean hasZoomLevel(int zoomLevel) {
        return Arrays.binarySearch(this.zoomLevels, zoomLevel) >= 0;
    }
}
