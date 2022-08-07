package gov.nasa.worldwind.layer.mercator.osm;

import java.util.Random;

import gov.nasa.worldwind.layer.mercator.MercatorTiledImageLayer;

public class OSMLayer extends MercatorTiledImageLayer {

    public static final String NAME = "OpenStreetMap";

    private final Random random = new Random();

    public OSMLayer() {
        super(NAME, 20, 3, 256, false);
    }

    @Override
    protected String getImageSourceUrl(int x, int y, int z) {
        char abc = "abc".charAt(random.nextInt(2));
        return "https://"+abc+".tile.openstreetmap.org/"+z+"/"+x+"/"+y+".png";
    }

}