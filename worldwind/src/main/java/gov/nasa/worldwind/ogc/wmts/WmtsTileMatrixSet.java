/*
 * Copyright (c) 2017 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.ogc.wmts;

import java.util.ArrayList;
import java.util.List;

import gov.nasa.worldwind.util.xml.XmlModel;

public class WmtsTileMatrixSet extends XmlModel {

    protected String identifier;

    protected String linkIdentifier;

    protected String supportedCrs;

    protected String wellKnownScaleSet;

    protected OwsBoundingBox boundingBox;

    protected List<WmtsTileMatrix> tileMatrices = new ArrayList<>();

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
        if (keyName.equals("Identifier")) {
            this.identifier = (String) value;
        } else if (keyName.equals("SupportedCRS")) {
            this.supportedCrs = (String) value;
        } else if (keyName.equals("WellKnownScaleSet")) {
            this.wellKnownScaleSet = (String) value;
        } else if (keyName.equals("BoundingBox")) {
            this.boundingBox = (OwsBoundingBox) value;
        } else if (keyName.equals("TileMatrix")) {
            this.tileMatrices.add((WmtsTileMatrix) value);
        }
    }

    @Override
    protected void parseText(String text) {
        this.linkIdentifier = text;
    }
}
