package gov.nasa.worldwind.layer.mercator.wiki;

import gov.nasa.worldwind.layer.mercator.MercatorTiledImageLayer;

public class WikiLayer extends MercatorTiledImageLayer {

    public enum Type { MAP, HYBRID }

    public static final String NAME = "Wiki";

    private final Type type;

    public WikiLayer(Type type) {
        super(NAME + type.name().toLowerCase(), 23, 3, 256, Type.HYBRID == type);
        this.type = type;
    }

    @Override
    protected String getImageSourceUrl(int x, int y, int z) {
        int i = x % 4 + y % 4 * 4;
        return "http://i"+i+".wikimapia.org/?lng=1&x="+x+"&y="+y+"&zoom="+z+"&type="+type.name().toLowerCase();
    }

}