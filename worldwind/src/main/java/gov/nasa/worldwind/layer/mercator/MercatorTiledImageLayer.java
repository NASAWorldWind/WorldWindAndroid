package gov.nasa.worldwind.layer.mercator;

import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.layer.RenderableLayer;
import gov.nasa.worldwind.render.ImageOptions;
import gov.nasa.worldwind.render.ImageSource;
import gov.nasa.worldwind.util.Level;
import gov.nasa.worldwind.util.LevelSet;
import gov.nasa.worldwind.util.Tile;
import gov.nasa.worldwind.util.TileFactory;

public abstract class MercatorTiledImageLayer extends RenderableLayer implements TileFactory {

    private static final double FULL_SPHERE = 360;

    private final int firstLevelOffset;

    public MercatorTiledImageLayer(String name, int numLevels, int firstLevelOffset, int tileSize, boolean overlay) {
        super(name);
        this.setPickEnabled(false);
        this.firstLevelOffset = firstLevelOffset;

        MercatorTiledSurfaceImage surfaceImage = new MercatorTiledSurfaceImage();
        surfaceImage.setLevelSet(new LevelSet(
                MercatorSector.fromDegrees(-1.0, 1.0, - FULL_SPHERE / 2, FULL_SPHERE / 2),
                FULL_SPHERE / (1 << firstLevelOffset), numLevels - firstLevelOffset, tileSize, tileSize));
        surfaceImage.setTileFactory(this);
        if(!overlay) {
            surfaceImage.setImageOptions(new ImageOptions(WorldWind.RGB_565)); // reduce memory usage by using a 16-bit configuration with no alpha
        }
        this.addRenderable(surfaceImage);
    }

    @Override
    public Tile createTile(Sector sector, Level level, int row, int column) {
        MercatorImageTile tile = new MercatorImageTile((MercatorSector) sector, level, row, column);
        tile.setImageSource(ImageSource.fromUrl(getImageSourceUrl(column, (1 << (level.levelNumber + firstLevelOffset)) - 1 - row, level.levelNumber + firstLevelOffset), tile));
        return tile;
    }

    protected abstract String getImageSourceUrl(int x, int y, int z);

}