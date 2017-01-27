/*
 * Copyright (c) 2017 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.ogc.wmts;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import gov.nasa.worldwind.util.xml.XmlModel;

public class WmtsLayer extends XmlModel {

    protected String identifier;

    protected String title;

    protected String layerAbstract;

    protected List<OwsBoundingBox> boundingBoxes = new ArrayList<>();

    protected OwsBoundingBox wgs84BoundingBox;

    protected List<WmtsElementLink> metadata = new ArrayList<>();

    protected List<WmtsStyle> styles = new ArrayList<>();

    protected List<String> formats = new ArrayList<>();

    protected List<String> infoFormats = new ArrayList<>();

    protected List<String> tileMatrixSetIds = new ArrayList<>();

    protected List<WmtsResourceUrl> resourceUrls = new ArrayList<>();

    protected List<WmtsDimension> dimensions = new ArrayList<>();

    public String getIdentifier() {
        return this.identifier;
    }

    public String getTitle() {
        return this.title;
    }

    public String getLayerAbstract() {
        return this.layerAbstract;
    }

    public OwsBoundingBox getWgs84BoundingBox() {
        return this.wgs84BoundingBox;
    }

    public List<OwsBoundingBox> getBoundingBoxes() {
        return Collections.unmodifiableList(this.boundingBoxes);
    }

    public List<WmtsElementLink> getMetadata() {
        return Collections.unmodifiableList(this.metadata);
    }

    public List<WmtsStyle> getStyles() {
        return Collections.unmodifiableList(this.styles);
    }

    public List<String> getFormats() {
        return Collections.unmodifiableList(this.formats);
    }

    public List<String> getInfoFormats() {
        return Collections.unmodifiableList(this.infoFormats);
    }

    public List<String> getTileMatrixSetIds() {
        return Collections.unmodifiableList(this.tileMatrixSetIds);
    }

    public List<WmtsResourceUrl> getResourceUrls() {
        return Collections.unmodifiableList(this.resourceUrls);
    }

    public WmtsCapabilities getCapabilities() {
        XmlModel parent = this.getParent();
        while (parent != null) {
            if (parent instanceof WmtsCapabilities) {
                return (WmtsCapabilities) parent;
            }
            parent = parent.getParent();
        }

        return null;
    }

    public List<WmtsTileMatrixSet> getTileMatrixSets() {
        WmtsCapabilities wmtsCapabilities = this.getCapabilities();
        List<WmtsTileMatrixSet> tileMatrixSets = wmtsCapabilities.getTileMatrixSets();
        List<WmtsTileMatrixSet> matchingSets = new ArrayList<>();
        for (WmtsTileMatrixSet tileMatrixSet : tileMatrixSets) {
            if (this.getTileMatrixSetIds().contains(tileMatrixSet.getIdentifier())) {
                matchingSets.add(tileMatrixSet);
            }
        }

        return matchingSets;
    }

    @Override
    protected void parseField(String keyName, Object value) {
        if (keyName.equals("Title")) {
            this.title = (String) value;
        } else if (keyName.equals("Abstract")) {
            this.layerAbstract = (String) value;
        } else if (keyName.equals("WGS84BoundingBox")) {
            this.wgs84BoundingBox = (OwsBoundingBox) value;
            this.wgs84BoundingBox.crs = "urn:ogc:def:crs:OGC:1.3:CRS84";
        } else if (keyName.equals("Identifier")) {
            this.identifier = (String) value;
        } else if (keyName.equals("Metadata")) {
            this.metadata.add((WmtsElementLink) value);
        } else if (keyName.equals("Style")) {
            this.styles.add((WmtsStyle) value);
        } else if (keyName.equals("Format")) {
            this.formats.add((String) value);
        } else if (keyName.equals("InfoFormat")) {
            this.infoFormats.add((String) value);
        } else if (keyName.equals("TileMatrixSetLink")) {
            this.tileMatrixSetIds.add(((WmtsTileMatrixSetLink) value).linkIdentifier);
        } else if (keyName.equals("ResourceURL")) {
            this.resourceUrls.add((WmtsResourceUrl) value);
        } else if (keyName.equals("BoundingBox")) {
            this.boundingBoxes.add((OwsBoundingBox) value);
        } else if (keyName.equals("Dimension")) {
            this.dimensions.add((WmtsDimension) value);
        }
    }
}
