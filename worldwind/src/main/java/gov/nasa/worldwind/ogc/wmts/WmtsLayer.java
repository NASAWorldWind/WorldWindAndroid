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

    protected String title;

    protected String layerAbstract;

    protected OwsBoundingBox boundingBox;

    protected String identifier;

    protected List<WmtsElementLink> metadata = new ArrayList<>();

    protected List<WmtsStyle> styles = new ArrayList<>();

    protected List<String> formats = new ArrayList<>();

    protected List<String> infoFormats = new ArrayList<>();

    protected List<String> tileMatrixSetIds = new ArrayList<>();

    protected List<WmtsResourceUrl> resourceUrls = new ArrayList<>();

    public String getIdentifier() {
        return this.identifier;
    }

    public String getTitle() {
        return this.title;
    }

    public String getLayerAbstract() {
        return this.layerAbstract;
    }

    public OwsBoundingBox getBoundingBox() {
        return this.boundingBox;
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

    @Override
    protected void parseField(String keyName, Object value) {
        if (keyName.equals("Title")) {
            this.title = (String) value;
        } else if (keyName.equals("Abstract")) {
            this.layerAbstract = (String) value;
        } else if (keyName.equals("WGS84BoundingBox")) {
            this.boundingBox = (OwsBoundingBox) value;
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
        }
    }
}
