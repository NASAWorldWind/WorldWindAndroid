/*
 * Copyright (c) 2017 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.ogc.wmts;

import java.util.ArrayList;
import java.util.List;

public class WmtsTileMatrixSet extends OwsDescription {

    protected String identifier;

    protected String linkIdentifier;

    protected String supportedCrs;

    protected String wellKnownScaleSet;

    protected OwsBoundingBox boundingBox;

    protected final List<WmtsTileMatrix> tileMatrices = new ArrayList<>();

    public WmtsTileMatrixSet() {
    }

    public String getIdentifier() {
        return this.identifier;
    }

    public String getLinkIdentifier() {
        return this.linkIdentifier;
    }

    public String getSupportedCrs() {
        return this.supportedCrs;
    }

    public String getWellKnownScaleSet() {
        return this.wellKnownScaleSet;
    }

    public OwsBoundingBox getBoundingBox() {
        return this.boundingBox;
    }

    public List<WmtsTileMatrix> getTileMatrices() {
        return this.tileMatrices;
    }

    @Override
    protected void parseField(String keyName, Object value) {
        super.parseField(keyName, value);
        switch (keyName) {
            case "Identifier":
                this.identifier = (String) value;
                break;
            case "SupportedCRS":
                this.supportedCrs = (String) value;
                break;
            case "WellKnownScaleSet":
                this.wellKnownScaleSet = (String) value;
                break;
            case "BoundingBox":
                this.boundingBox = (OwsBoundingBox) value;
                break;
            case "TileMatrix":
                this.tileMatrices.add((WmtsTileMatrix) value);
                break;
        }
    }

    @Override
    protected void parseText(String text) {
        this.linkIdentifier = text;
    }
}
