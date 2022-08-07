package gov.nasa.worldwind.layer.mercator.osm;

import java.util.Random;

import gov.nasa.worldwind.layer.mercator.MercatorTiledImageLayer;

public class OTMLayer extends MercatorTiledImageLayer {

    public static final String NAME = "OpenTopoMap";

    private final Random random = new Random();

    public OTMLayer() {
        super(NAME, 18, 3, 256, false);
    }

    @Override
    protected String getImageSourceUrl(int x, int y, int z) {
        char abc = "abc".charAt(random.nextInt(2));
        return "https://"+abc+".tile.opentopomap.org/"+z+"/"+x+"/"+y+".png";
    }

}