/*
 * Copyright (c) 2017 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.ogc.wmts;

public class WmtsTileMatrix extends OwsDescription {

    protected String identifier;

    protected String limitIdentifier;

    protected double scaleDenominator;

    protected String topLeftCorner;

    protected int tileWidth;

    protected int tileHeight;

    protected int matrixWidth;

    protected int matrixHeight;

    public WmtsTileMatrix() {
    }

    public String getIdentifier() {
        return this.identifier;
    }

    public double getScaleDenominator() {
        return this.scaleDenominator;
    }

    public String getTopLeftCorner() {
        return this.topLeftCorner;
    }

    public int getTileWidth() {
        return this.tileWidth;
    }

    public int getTileHeight() {
        return this.tileHeight;
    }

    public int getMatrixWidth() {
        return this.matrixWidth;
    }

    public int getMatrixHeight() {
        return this.matrixHeight;
    }

    public String getLimitIdentifier() {
        return this.limitIdentifier;
    }

    @Override
    protected void parseField(String keyName, Object value) {
        super.parseField(keyName, value);
        switch (keyName) {
            case "Identifier":
                this.identifier = (String) value;
                break;
            case "ScaleDenominator":
                this.scaleDenominator = Double.parseDouble((String) value);
                break;
            case "TopLeftCorner":
                this.topLeftCorner = (String) value;
                break;
            case "TileWidth":
                this.tileWidth = Integer.parseInt((String) value);
                break;
            case "TileHeight":
                this.tileHeight = Integer.parseInt((String) value);
                break;
            case "MatrixWidth":
                this.matrixWidth = Integer.parseInt((String) value);
                break;
            case "MatrixHeight":
                this.matrixHeight = Integer.parseInt((String) value);
                break;
        }
    }

    @Override
    protected void parseText(String text) {
        this.limitIdentifier = text;
    }
}
