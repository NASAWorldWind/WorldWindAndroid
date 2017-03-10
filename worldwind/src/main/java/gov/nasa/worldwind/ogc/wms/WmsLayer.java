/*
 * Copyright (c) 2017 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.ogc.wms;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.util.xml.XmlModel;

public class WmsLayer extends XmlModel {

    // Properties of the Layer element
    protected List<WmsLayer> layers = new ArrayList<>();

    protected String name;

    protected String title;

    protected String description;

    protected List<String> keywordList = new ArrayList<>();

    protected List<WmsStyle> styles = new ArrayList<>();

    // The 1.3.0 Reference System
    protected List<String> crses = new ArrayList<>();

    // The 1.1.1 Reference System
    protected List<String> srses = new ArrayList<>();

    protected WmsGeographicBoundingBox geographicBoundingBox;

    protected List<WmsBoundingBox> boundingBoxes = new ArrayList<>();

    // The 1.3.0 Dimension Property
    protected List<WmsDimension> dimensions = new ArrayList<>();

    // The 1.1.1 Dimension Property
    protected List<WmsDimension> extents = new ArrayList<>();

    protected WmsAttribution attribution;

    protected List<WmsAuthorityUrl> authorityUrls = new ArrayList<>();

    protected List<WmsIdentifier> identifiers = new ArrayList<>();

    protected List<WmsInfoUrl> metadataUrls = new ArrayList<>();

    protected List<WmsInfoUrl> dataUrls = new ArrayList<>();

    protected List<WmsInfoUrl> featureListUrls = new ArrayList<>();

    // The 1.3.0 Scale Property
    protected Double maxScaleDenominator;

    // The 1.3.0 Scale Property
    protected Double minScaleDenominator;

    // The 1.1.1 Scale Property
    protected WmsScaleHint scaleHint;

    // Properties of the Layer attributes
    protected boolean queryable;

    protected Integer cascaded;

    protected Boolean opaque;

    protected Boolean noSubsets;

    protected Integer fixedWidth;

    protected Integer fixedHeight;

    public WmsLayer() {
    }

    protected List<WmsLayer> getNamedLayers() {
        List<WmsLayer> namedLayers = new ArrayList<>();

        if (this.getName() != null)
            namedLayers.add(this);

        for (WmsLayer layer : this.getLayers()) {
            namedLayers.addAll(layer.getNamedLayers());
        }

        return namedLayers;
    }

    public List<WmsStyle> getStyles() {
        XmlModel parent = this.getParent();

        while (parent != null) {
            if (parent instanceof WmsLayer) {
                this.styles.addAll(((WmsLayer) parent).styles);
                break;
            }
            parent = parent.getParent();
        }

        return this.styles;
    }

    public WmsStyle getStyle(String name) {
        if (name == null || name.isEmpty()) {
            return null;
        }

        for (WmsStyle style : this.getStyles()) {
            if (style.getName().equals(name))
                return style;
        }

        return null;
    }

    public WmsScaleHint getScaleHint() {
        XmlModel parent = this.getParent();

        while (parent != null) {
            if (parent instanceof WmsLayer) {
                WmsLayer wmsLayer = (WmsLayer) parent;
                if (wmsLayer.scaleHint != null) {
                    return wmsLayer.scaleHint;
                }
            }
            parent = parent.getParent();
        }

        return new WmsScaleHint(); // to prevent NPE on chained calls
    }

    public Double getMaxScaleDenominator() {
        Double actualMaxScaleDenominator = this.maxScaleDenominator;

        XmlModel parent = this.getParent();

        while (actualMaxScaleDenominator == null && parent != null) {
            if (parent instanceof WmsLayer) {
                actualMaxScaleDenominator = ((WmsLayer) parent).maxScaleDenominator;
            }
            parent = parent.getParent();
        }

        return actualMaxScaleDenominator;
    }

    public Double getMinScaleDenominator() {
        Double actualMinScaleDenominator = this.minScaleDenominator;

        XmlModel parent = this.getParent();

        while (actualMinScaleDenominator == null && parent != null) {
            if (parent instanceof WmsLayer) {
                actualMinScaleDenominator = ((WmsLayer) parent).minScaleDenominator;
            }
            parent = parent.getParent();
        }

        return actualMinScaleDenominator;
    }

    public List<WmsDimension> getDimensions() {
        XmlModel parent = this.getParent();

        Map<String, WmsDimension> dimensionMap = new HashMap<>();

        while (parent != null) {
            if (parent instanceof WmsLayer) {
                for (WmsDimension dimension : ((WmsLayer) parent).dimensions) {
                    if (!dimensionMap.containsKey(dimension.getName())) {
                        dimensionMap.put(dimension.getName(), dimension);
                    }
                }
            }
            parent = parent.getParent();
        }

        return new ArrayList<>(dimensionMap.values());
    }

    public List<WmsDimension> getExtents() {
        XmlModel parent = this.getParent();

        while (parent != null) {
            if (parent instanceof WmsLayer) {
                this.extents.addAll(((WmsLayer) parent).extents);
            }
            parent = parent.getParent();
        }

        return this.extents;
    }

    public Integer getCascaded() {
        Integer actualCascade = this.cascaded;

        XmlModel parent = this.getParent();

        while (actualCascade == null && parent != null) {
            if (parent instanceof WmsLayer) {
                actualCascade = ((WmsLayer) parent).cascaded;
            }
            parent = parent.getParent();
        }

        return actualCascade;
    }

    public Integer getFixedHeight() {
        Integer actualFixedHeight = this.fixedHeight;

        XmlModel parent = this.getParent();

        while (actualFixedHeight == null && parent != null) {
            if (parent instanceof WmsLayer) {
                actualFixedHeight = ((WmsLayer) parent).fixedHeight;
            }
            parent = parent.getParent();
        }

        return actualFixedHeight;
    }

    public Integer getFixedWidth() {
        Integer actualFixedWidth = this.fixedWidth;

        XmlModel parent = this.getParent();

        while (actualFixedWidth == null && parent != null) {
            if (parent instanceof WmsLayer) {
                actualFixedWidth = ((WmsLayer) parent).fixedWidth;
            }
            parent = parent.getParent();
        }

        return actualFixedWidth;
    }

    public Boolean isNoSubsets() {
        Boolean actualNoSubsets = this.noSubsets;

        XmlModel parent = this.getParent();

        while (actualNoSubsets == null && parent != null) {
            if (parent instanceof WmsLayer) {
                actualNoSubsets = ((WmsLayer) parent).noSubsets;
            }
            parent = parent.getParent();
        }

        return actualNoSubsets;
    }

    public Boolean isOpaque() {
        Boolean actualOpaque = this.opaque;

        XmlModel parent = this.getParent();

        while (actualOpaque == null && parent != null) {
            if (parent instanceof WmsLayer) {
                actualOpaque = ((WmsLayer) parent).opaque;
            }
            parent = parent.getParent();
        }

        return actualOpaque;
    }

    public boolean isQueryable() {
        return this.queryable;
    }

    public WmsAttribution getAttribution() {
        WmsAttribution actualAttribution = this.attribution;

        XmlModel parent = this.getParent();

        while (actualAttribution == null && parent != null) {
            if (parent instanceof WmsLayer) {
                actualAttribution = ((WmsLayer) parent).attribution;
            }
            parent = parent.getParent();
        }

        return actualAttribution;
    }

    public List<WmsAuthorityUrl> getAuthorityUrls() {
        XmlModel parent = this.getParent();

        while (parent != null) {
            if (parent instanceof WmsLayer) {
                this.authorityUrls.addAll(((WmsLayer) parent).authorityUrls);
                break;
            }
            parent = parent.getParent();
        }

        return this.authorityUrls;
    }

    public List<WmsIdentifier> getIdentifiers() {
        return this.identifiers;
    }

    public List<WmsInfoUrl> getMetadataUrls() {
        return this.metadataUrls;
    }

    public List<WmsInfoUrl> getFeatureListUrls() {
        return this.featureListUrls;
    }

    public List<WmsInfoUrl> getDataUrls() {
        return this.dataUrls;
    }

    public List<WmsLayer> getLayers() {
        return this.layers;
    }

    public List<WmsBoundingBox> getBoundingBoxes() {
        XmlModel parent = this.getParent();

        Map<String, WmsBoundingBox> boundingBoxMap = new HashMap<>();

        while (parent != null) {
            if (parent instanceof WmsLayer) {
                for (WmsBoundingBox boundingBox : ((WmsLayer) parent).boundingBoxes) {
                    if (!boundingBoxMap.containsKey(boundingBox.getCRS())) {
                        boundingBoxMap.put(boundingBox.getCRS(), boundingBox);
                    }
                }
            }
            parent = parent.getParent();
        }

        return new ArrayList<>(boundingBoxMap.values());
    }

    public Sector getGeographicBoundingBox() {
        WmsGeographicBoundingBox actualGeographicBoundingBox = this.geographicBoundingBox;

        XmlModel parent = this.getParent();

        while (actualGeographicBoundingBox == null && parent != null) {
            if (parent instanceof WmsLayer) {
                actualGeographicBoundingBox = ((WmsLayer) parent).geographicBoundingBox;
            }
            parent = parent.getParent();
        }

        if (actualGeographicBoundingBox != null) {
            return actualGeographicBoundingBox.getGeographicBoundingBox();
        }
        return null;
    }

    public List<String> getKeywordList() {
        return this.keywordList;
    }

    public String getAbstract() {
        return this.description;
    }

    public String getName() {
        return this.name;
    }

    public String getTitle() {
        return this.title;
    }

    public List<String> getSrses() {
        XmlModel parent = this.getParent();

        while (parent != null) {
            if (parent instanceof WmsLayer) {
                this.srses.addAll(((WmsLayer) parent).srses);
            }
            parent = parent.getParent();
        }

        return this.srses;
    }

    public List<String> getCrses() {
        XmlModel parent = this.getParent();

        while (parent != null) {
            if (parent instanceof WmsLayer) {
                this.crses.addAll(((WmsLayer) parent).crses);
            }
            parent = parent.getParent();
        }

        return this.crses;
    }

    /**
     * Provides a WMS version agnostic reference system by returning the non-null reference system set. It is the
     * responsibility of the caller to understand which WMS version is being used.
     *
     * @return a set of reference systems supported by this layer
     */
    public List<String> getReferenceSystems() {
        List<String> rs = this.getCrses();

        if (rs == null || rs.size() == 0) {
            rs = this.getSrses();
        }

        return rs;
    }

    public WmsCapability getCapability() {
        XmlModel model = this;

        while (model != null) {
            model = model.getParent();
            if (model instanceof WmsCapability) {
                return (WmsCapability) model;
            }
        }

        return null;
    }

    @Override
    public void parseField(String keyName, Object value) {
        if (keyName.equals("Layer")) {
            this.layers.add((WmsLayer) value);
        } else if (keyName.equals("Name")) {
            this.name = (String) value;
        } else if (keyName.equals("Title")) {
            this.title = (String) value;
        } else if (keyName.equals("Abstract")) {
            this.description = (String) value;
        } else if (keyName.equals("KeywordList")) {
            this.keywordList.addAll(((WmsKeywords) value).getKeywords());
        } else if (keyName.equals("Style")) {
            this.styles.add((WmsStyle) value);
        } else if (keyName.equals("CRS")) {
            this.crses.add((String) value);
        } else if (keyName.equals("SRS")) {
            this.srses.add((String) value);
        } else if (keyName.equals("EX_GeographicBoundingBox")) {
            this.geographicBoundingBox = (WmsGeographicBoundingBox) value;
        } else if (keyName.equals("LatLonBoundingBox")) {
            this.geographicBoundingBox = (WmsGeographicBoundingBox) value;
        } else if (keyName.equals("BoundingBox")) {
            this.boundingBoxes.add((WmsBoundingBox) value);
        } else if (keyName.equals("Dimension")) {
            this.dimensions.add((WmsDimension) value);
        } else if (keyName.equals("Extent")) {
            this.extents.add((WmsDimension) value);
        } else if (keyName.equals("Attribution")) {
            this.attribution = (WmsAttribution) value;
        } else if (keyName.equals("AuthorityURL")) {
            this.authorityUrls.add((WmsAuthorityUrl) value);
        } else if (keyName.equals("Identifier")) {
            this.identifiers.add((WmsIdentifier) value);
        } else if (keyName.equals("MetadataURL")) {
            this.metadataUrls.add((WmsInfoUrl) value);
        } else if (keyName.equals("DataURL")) {
            this.dataUrls.add((WmsInfoUrl) value);
        } else if (keyName.equals("FeatureListURL")) {
            this.featureListUrls.add((WmsInfoUrl) value);
        } else if (keyName.equals("MinScaleDenominator")) {
            this.minScaleDenominator = Double.parseDouble((String) value);
        } else if (keyName.equals("MaxScaleDenominator")) {
            this.maxScaleDenominator = Double.parseDouble((String) value);
        } else if (keyName.equals("ScaleHint")) {
            this.scaleHint = (WmsScaleHint) value;
        } else if (keyName.equals("queryable")) {
            this.queryable = Boolean.parseBoolean((String) value);
        } else if (keyName.equals("cascaded")) {
            this.cascaded = Integer.parseInt((String) value);
        } else if (keyName.equals("opaque")) {
            this.opaque = Boolean.parseBoolean((String) value);
        } else if (keyName.equals("noSubsets")) {
            this.noSubsets = Boolean.parseBoolean((String) value);
        } else if (keyName.equals("fixedWidth")) {
            this.fixedWidth = Integer.parseInt((String) value);
        } else if (keyName.equals("fixedHeight")) {
            this.fixedHeight = Integer.parseInt((String) value);
        }
    }
}