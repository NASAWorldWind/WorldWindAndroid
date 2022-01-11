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
    protected final List<WmsLayer> layers = new ArrayList<>();

    protected String name;

    protected String title;

    protected String description;

    protected final List<String> keywordList = new ArrayList<>();

    protected final List<WmsStyle> styles = new ArrayList<>();

    // The 1.3.0 Reference System
    protected final List<String> crses = new ArrayList<>();

    // The 1.1.1 Reference System
    protected final List<String> srses = new ArrayList<>();

    protected WmsGeographicBoundingBox geographicBoundingBox;

    protected final List<WmsBoundingBox> boundingBoxes = new ArrayList<>();

    // The 1.3.0 Dimension Property
    protected final List<WmsDimension> dimensions = new ArrayList<>();

    // The 1.1.1 Dimension Property
    protected final List<WmsDimension> extents = new ArrayList<>();

    protected WmsAttribution attribution;

    protected final List<WmsAuthorityUrl> authorityUrls = new ArrayList<>();

    protected final List<WmsIdentifier> identifiers = new ArrayList<>();

    protected final List<WmsInfoUrl> metadataUrls = new ArrayList<>();

    protected final List<WmsInfoUrl> dataUrls = new ArrayList<>();

    protected final List<WmsInfoUrl> featureListUrls = new ArrayList<>();

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
        switch (keyName) {
            case "Layer":
                this.layers.add((WmsLayer) value);
                break;
            case "Name":
                this.name = (String) value;
                break;
            case "Title":
                this.title = (String) value;
                break;
            case "Abstract":
                this.description = (String) value;
                break;
            case "KeywordList":
                this.keywordList.addAll(((WmsKeywords) value).getKeywords());
                break;
            case "Style":
                this.styles.add((WmsStyle) value);
                break;
            case "CRS":
                this.crses.add((String) value);
                break;
            case "SRS":
                this.srses.add((String) value);
                break;
            case "EX_GeographicBoundingBox":
            case "LatLonBoundingBox":
                this.geographicBoundingBox = (WmsGeographicBoundingBox) value;
                break;
            case "BoundingBox":
                this.boundingBoxes.add((WmsBoundingBox) value);
                break;
            case "Dimension":
                this.dimensions.add((WmsDimension) value);
                break;
            case "Extent":
                this.extents.add((WmsDimension) value);
                break;
            case "Attribution":
                this.attribution = (WmsAttribution) value;
                break;
            case "AuthorityURL":
                this.authorityUrls.add((WmsAuthorityUrl) value);
                break;
            case "Identifier":
                this.identifiers.add((WmsIdentifier) value);
                break;
            case "MetadataURL":
                this.metadataUrls.add((WmsInfoUrl) value);
                break;
            case "DataURL":
                this.dataUrls.add((WmsInfoUrl) value);
                break;
            case "FeatureListURL":
                this.featureListUrls.add((WmsInfoUrl) value);
                break;
            case "MinScaleDenominator":
                this.minScaleDenominator = Double.parseDouble((String) value);
                break;
            case "MaxScaleDenominator":
                this.maxScaleDenominator = Double.parseDouble((String) value);
                break;
            case "ScaleHint":
                this.scaleHint = (WmsScaleHint) value;
                break;
            case "queryable":
                this.queryable = Boolean.parseBoolean((String) value);
                break;
            case "cascaded":
                this.cascaded = Integer.parseInt((String) value);
                break;
            case "opaque":
                this.opaque = Boolean.parseBoolean((String) value);
                break;
            case "noSubsets":
                this.noSubsets = Boolean.parseBoolean((String) value);
                break;
            case "fixedWidth":
                this.fixedWidth = Integer.parseInt((String) value);
                break;
            case "fixedHeight":
                this.fixedHeight = Integer.parseInt((String) value);
                break;
        }
    }
}