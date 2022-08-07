package gov.nasa.worldwind.layer.mercator.google;

import gov.nasa.worldwind.layer.mercator.MercatorTiledImageLayer;

public class GoogleLayer extends MercatorTiledImageLayer {

    public GoogleLayer(Type type) {
        super(type.layerName, 22, 0, 256, type.overlay);
        this.lyrs = type.lyrs;
    }

    private final String lyrs;

    public enum Type {
        ROADMAP("Google road map", "m", false),
        ROADMAP2("Google road map 2", "r", false),
        TERRAIN("Google map w/ terrain", "p", false),
        TERRAIN_ONLY("Google terrain only", "t", false),
        HYBRID("Google hybrid", "y", false),
        SATELLITE("Google satellite", "s", false),
        ROADS("Google roads", "h", true),
        TRAFFIC("Google traffic", "h,traffic&style=15", true);

        private final String layerName;
        private final String lyrs;
        private final boolean overlay;

        Type(String layerName, String lyrs, boolean overlay) {
            this.layerName = layerName;
            this.lyrs = lyrs;
            this.overlay = overlay;
        }
    }

    @Override
    public String getImageSourceUrl(int x, int y, int z) {
        return "https://mt.google.com/vt/lyrs="+lyrs+"&x="+x+"&y="+y+"&z="+z+"&hl=ru";
    }
}