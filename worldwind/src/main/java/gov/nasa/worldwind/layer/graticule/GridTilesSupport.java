package gov.nasa.worldwind.layer.graticule;

import android.graphics.Rect;

import java.util.ArrayList;
import java.util.List;

import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.render.RenderContext;

class GridTilesSupport {

    interface Callback {
        AbstractGraticuleTile[][] initGridTiles(int rows, int cols);
        AbstractGraticuleTile createGridTile(Sector sector);
        Sector getGridSector(int row, int col);
        int getGridColumn(double longitude);
        int getGridRow(double latitude);
    }

    private final Callback callback;
    private final int rows;
    private final int cols;
    private final AbstractGraticuleTile[][] gridTiles;

    GridTilesSupport(Callback callback, int rows, int cols) {
        this.callback = callback;
        this.rows = rows;
        this.cols = cols;
        this.gridTiles = callback.initGridTiles(rows, cols);
    }

    void clearTiles() {
        for (int row = 0; row < this.rows; row++) {
            for (int col = 0; col < this.cols; col++) {
                if (this.gridTiles[row][col] != null) {
                    this.gridTiles[row][col].clearRenderables();
                    this.gridTiles[row][col] = null;
                }
            }
        }
    }

    /**
     * Select the visible grid elements
     *
     * @param rc the current <code>RenderContext</code>.
     */
    void selectRenderables(RenderContext rc) {
        List<AbstractGraticuleTile> tileList = getVisibleTiles(rc);
        if (tileList.size() > 0) {
            for (AbstractGraticuleTile gt : tileList) {
                // Select tile visible elements
                gt.selectRenderables(rc);
            }
        }
    }

    private List<AbstractGraticuleTile> getVisibleTiles(RenderContext rc) {
        List<AbstractGraticuleTile> tileList = new ArrayList<>();
        Sector vs = rc.terrain.getSector();
        if (vs != null) {
            Rect gridRectangle = getGridRectangleForSector(vs);
            for (int row = gridRectangle.top; row <= gridRectangle.bottom; row++) {
                for (int col = gridRectangle.left; col <= gridRectangle.right; col++) {
                    if (gridTiles[row][col] == null)
                        gridTiles[row][col] = callback.createGridTile(callback.getGridSector(row, col));
                    if (gridTiles[row][col].isInView(rc))
                        tileList.add(gridTiles[row][col]);
                    else
                        gridTiles[row][col].clearRenderables();
                }
            }
        }
        return tileList;
    }

    private Rect getGridRectangleForSector(Sector sector) {
        int x1 = callback.getGridColumn(sector.minLongitude());
        int x2 = callback.getGridColumn(sector.maxLongitude());
        int y1 = callback.getGridRow(sector.minLatitude());
        int y2 = callback.getGridRow(sector.maxLatitude());
        return new Rect(x1, y1, x2, y2);
    }

}
