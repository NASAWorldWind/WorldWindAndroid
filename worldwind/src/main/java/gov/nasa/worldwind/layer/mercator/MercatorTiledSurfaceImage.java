package gov.nasa.worldwind.layer.mercator;

import gov.nasa.worldwind.shape.TiledSurfaceImage;
import gov.nasa.worldwind.util.Level;

public class MercatorTiledSurfaceImage extends TiledSurfaceImage {

    @Override
    protected void createTopLevelTiles() {
        Level firstLevel = this.levelSet.firstLevel();
        if (firstLevel != null) {
            MercatorImageTile.assembleMercatorTilesForLevel(firstLevel, this.tileFactory, this.topLevelTiles);
        }
    }

}