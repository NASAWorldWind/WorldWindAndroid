/*
 * Copyright (c) 2017 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.ogc.wmts;

import java.util.LinkedHashSet;
import java.util.Set;

import gov.nasa.worldwind.util.xml.XmlModel;

public class WmtsTileMatrixSet extends XmlModel {

    protected String identifier;

    protected String linkIdentifier;

    protected String supportedCrs;

    protected String wellKnownScaleSet;

    protected OwsBoundingBox boundingBox;

    protected Set<WmtsTileMatrix> tileMatrices = new LinkedHashSet<>();

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
