/*
 * Copyright (c) 2016 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.ogc.wms;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.xml.namespace.QName;

import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.util.LevelSetConfig;
import gov.nasa.worldwind.util.xml.DoubleModel;
import gov.nasa.worldwind.util.xml.XmlModel;

public class WmsLayerCapabilities extends XmlModel {

    protected QName layerAbstract;

    protected QName attribution;

    protected QName authorityUrl;

    protected QName boundingBox;

    protected QName crs;

    protected QName dataUrl;

    protected QName dimension;

    protected QName extent;

//    protected QName extremeElevations;

    protected QName featureListUrl;

    protected QName geographicBoundingBox;

    protected QName identifier;

    protected QName keywordList;

    protected QName keyword;

    protected QName lastUpdate;

    protected QName latLonBoundingBox; // 1.1.1

    protected QName layer;

    protected QName maxScaleDenominator;

    protected QName metadataUrl;

    protected QName minScaleDenominator;

    protected QName name;

    protected QName scaleHint;

    protected QName srs;

    protected QName style;

    protected QName title;

    protected QName queryable;

    protected QName opaque;

    protected QName noSubsets;

    protected QName fixedWidth;

    protected QName fixedHeight;

    protected QName cascaded;

    public WmsLayerCapabilities(String namespaceURI) {
        super(namespaceURI);

        this.initialize();
    }

    private void initialize() {
        this.layerAbstract = new QName(this.getNamespaceUri(), "Abstract");
        this.attribution = new QName(this.getNamespaceUri(), "Attribution");
        this.authorityUrl = new QName(this.getNamespaceUri(), "AuthorityURL");
        this.boundingBox = new QName(this.getNamespaceUri(), "BoundingBox");
        this.crs = new QName(this.getNamespaceUri(), "CRS");
        this.dataUrl = new QName(this.getNamespaceUri(), "DataURL");
        this.dimension = new QName(this.getNamespaceUri(), "Dimension");
        this.extent = new QName(this.getNamespaceUri(), "Extent");
//        this.extremeElevations = new QName(this.getNamespaceUri(), "ExtremeElevations");
        this.featureListUrl = new QName(this.getNamespaceUri(), "FeatureListURL");
        this.geographicBoundingBox = new QName(this.getNamespaceUri(), "EX_GeographicBoundingBox");
        this.identifier = new QName(this.getNamespaceUri(), "Identifier");
        this.keywordList = new QName(this.getNamespaceUri(), "KeywordList");
        this.keyword = new QName(this.getNamespaceUri(), "Keyword");
        this.lastUpdate = new QName(this.getNamespaceUri(), "LastUpdate");
        this.latLonBoundingBox = new QName(this.getNamespaceUri(), "LatLonBoundingBox");
        this.layer = new QName(this.getNamespaceUri(), "Layer");
        this.maxScaleDenominator = new QName(this.getNamespaceUri(), "MaxScaleDenominator");
        this.metadataUrl = new QName(this.getNamespaceUri(), "MetadataURL");
        this.minScaleDenominator = new QName(this.getNamespaceUri(), "MinScaleDenominator");
        this.name = new QName(this.getNamespaceUri(), "Name");
        this.scaleHint = new QName(this.getNamespaceUri(), "ScaleHint");
        this.srs = new QName(this.getNamespaceUri(), "SRS");
        this.style = new QName(this.getNamespaceUri(), "Style");
        this.title = new QName(this.getNamespaceUri(), "Title");
        this.queryable = new QName("", "queryable");
        this.noSubsets = new QName("", "noSubsets");
        this.fixedWidth = new QName("", "fixedWidth");
        this.fixedHeight = new QName("", "fixedHeight");
        this.cascaded = new QName("", "cascaded");
        this.opaque = new QName("", "opaque");
    }

    public List<WmsLayerCapabilities> getNamedLayers() {
        List<WmsLayerCapabilities> namedLayers = new ArrayList<WmsLayerCapabilities>();

        if (this.getName() != null)
            namedLayers.add(this);

        for (WmsLayerCapabilities layer : this.getLayers()) {
            namedLayers.addAll(layer.getNamedLayers());
        }

        return namedLayers;
    }

    public WmsLayerCapabilities getLayerByName(String name) {
        if (name == null || name.isEmpty()) {
            return null;
        }

        if (this.getName() != null && this.getName().equals(name))
            return this;

        for (WmsLayerCapabilities lc : this.getLayers()) {
            if (lc.getName() != null && lc.getName().equals(name))
                return lc;
        }

        return null;
    }

    public WmsLayerStyle getStyleByName(String name) {
        if (name == null || name.isEmpty()) {
            return null;
        }

        for (WmsLayerStyle style : this.getStyles()) {
            if (style.getName().equals(name))
                return style;
        }

        return null;
    }

    public String getLastUpdate() {
        return this.getChildCharacterValue(this.lastUpdate);
    }

    public Double getMinScaleHint() {
        Object o = this.getInheritedField(this.scaleHint);
        if (o != null && o instanceof XmlModel) {
            XmlModel scaleHint = (XmlModel) o;
            return scaleHint.getDoubleAttributeValue(new QName("", "min"), true);
        }
        return null;
    }

    public Double getMaxScaleHint() {
        Object o = this.getInheritedField(this.scaleHint);
        if (o != null && o instanceof XmlModel) {
            XmlModel scaleHint = (XmlModel) o;
            return scaleHint.getDoubleAttributeValue(new QName("", "max"), true);
        }
        return null;
    }

    public Set<WmsLayerDimension> getDimensions() {
        Set<WmsLayerDimension> dimensions = (Set<WmsLayerDimension>) this.getInheritedField(this.dimension);
        return dimensions != null ? dimensions : null;
    }

    public Set<WmsLayerExtent> getExtents() {
        Set<WmsLayerExtent> extents = (Set<WmsLayerExtent>) this.getField(this.extent);
        return extents != null ? extents : Collections.<WmsLayerExtent>emptySet();
    }

    public Boolean getCascaded() {
        return this.getBooleanAttributeValue(this.cascaded, true);
    }

    public Integer getFixedHeight() {
        return this.getIntegerAttributeValue(this.fixedHeight, true);
    }

    public Integer getFixedWidth() {
        return this.getIntegerAttributeValue(this.fixedWidth, true);
    }

    public Boolean isNoSubsets() {
        return this.getBooleanAttributeValue(this.noSubsets, true);
    }

    public Boolean isOpaque() {
        return this.getBooleanAttributeValue(this.opaque, true);
    }

    public Boolean isQueryable() {
        return this.getBooleanAttributeValue(this.queryable, true);
    }

    public Set<WmsLayerAttribution> getAttributions() {
        Set<WmsLayerAttribution> attributions = (Set<WmsLayerAttribution>) this.getInheritedField(this.attribution);
        return attributions != null ? attributions : Collections.<WmsLayerAttribution>emptySet();
    }

    public Set<WmsAuthorityUrl> getAuthorityUrls() {
        Set<WmsAuthorityUrl> authorityUrls = new HashSet<>();
        this.getAdditiveInheritedField(this.authorityUrl, authorityUrls);
        return authorityUrls;
    }

    public Set<WmsLayerIdentifier> getIdentifiers() {
        Set<WmsLayerIdentifier> identifiers = (Set<WmsLayerIdentifier>) this.getInheritedField(this.identifier);
        return identifiers != null ? identifiers : Collections.<WmsLayerIdentifier>emptySet();
    }

    public Set<WmsLayerInfoUrl> getMetadataUrls() {
        Set<WmsLayerInfoUrl> metadataUrls = (Set<WmsLayerInfoUrl>) this.getField(this.metadataUrl);
        return metadataUrls != null ? metadataUrls : metadataUrls;
    }

    public Set<WmsLayerInfoUrl> getFeatureListUrls() {
        Set<WmsLayerInfoUrl> featureUrls = (Set<WmsLayerInfoUrl>) this.getField(this.featureListUrl);
        return featureUrls != null ? featureUrls : Collections.<WmsLayerInfoUrl>emptySet();
    }

    public Set<WmsLayerInfoUrl> getDataUrls() {
        Set<WmsLayerInfoUrl> dataUrls = (Set<WmsLayerInfoUrl>) this.getField(this.dataUrl);
        return dataUrls != null ? dataUrls : Collections.<WmsLayerInfoUrl>emptySet();
    }

    public List<WmsLayerCapabilities> getLayers() {
        List<WmsLayerCapabilities> layers = (List<WmsLayerCapabilities>) this.getField(this.layer);
        return layers != null ? layers : Collections.<WmsLayerCapabilities>emptyList();
    }

    public Set<WmsLayerStyle> getStyles() {
        Set<WmsLayerStyle> styles = new HashSet<>();
        this.getAdditiveInheritedField(this.style, styles);
        return styles;
    }

    public Set<WmsBoundingBox> getBoundingBoxes() {
        Set<WmsBoundingBox> boundingBoxes = (Set<WmsBoundingBox>) this.getInheritedField(this.boundingBox);
        return boundingBoxes != null ? boundingBoxes : Collections.<WmsBoundingBox>emptySet();
    }

    public Sector getGeographicBoundingBox() {
        WmsGeographicBoundingBox boundingBox = (WmsGeographicBoundingBox) this.getInheritedField(this.geographicBoundingBox);

        if (boundingBox == null) {
            // try the 1.1.1 style
            boundingBox = (WmsGeographicBoundingBox) this.getInheritedField(this.latLonBoundingBox);
        }

        if (boundingBox != null) {
            Double minLon = boundingBox.getWestBound();
            Double maxLon = boundingBox.getEastBound();
            Double minLat = boundingBox.getSouthBound();
            Double maxLat = boundingBox.getNorthBound();
            if (minLon == null || maxLon == null || minLat == null || maxLat == null) {
                return null;
            }
            return Sector.fromDegrees(minLat, minLon, maxLat - minLat, maxLon - minLon);
        }
        return null;
    }

    public Set<String> getKeywords() {
        WmsKeywords keywords = (WmsKeywords) this.getField(this.keywordList);
        if (keywords != null) {
            return keywords.getKeywords();
        } else {
            return Collections.emptySet();
        }
    }

    public String getLayerAbstract() {
        return this.getChildCharacterValue(this.layerAbstract);
    }

    public Double getMaxScaleDenominator() {
        DoubleModel model = (DoubleModel) this.getInheritedField(this.maxScaleDenominator);
        if (model != null) {
            return model.getValue();
        } else {
            return null;
        }
    }

    public Double getMinScaleDenominator() {
        DoubleModel model = (DoubleModel) this.getInheritedField(this.minScaleDenominator);
        if (model != null) {
            return model.getValue();
        } else {
            return null;
        }
    }

    public String getName() {
        return this.getChildCharacterValue(this.name);
    }

    protected void setName(String name) {
        this.setChildCharacterValue(this.name, name);
    }

    public String getTitle() {
        return this.getChildCharacterValue(this.title);
    }

    protected void setTitle(String title) {
        this.setChildCharacterValue(this.title, title);
    }

    public Set<String> getSRS() {
        Set<String> srs = new HashSet<>();
        this.getAdditiveInheritedField(this.srs, srs);
        return srs;
    }

    public Set<String> getCRS() {
        Set<String> crs = new HashSet<>();
        this.getAdditiveInheritedField(this.crs, crs);
        return crs;
    }

    /**
     * Provides a WMS version agnostic reference system by returning the non-null reference system set. It is the
     * responsibility of the caller to understand which WMS version is being used.
     *
     * @return
     */
    public Set<String> getReferenceSystem() {

        Set<String> rs = this.getCRS();

        if (rs.size() == 0) {
            rs = this.getSRS();
        }

        return rs;
    }

    public boolean hasCoordinateSystem(String coordSys) {
        if (coordSys == null)
            return false;

        Set<String> crs = this.getCRS();
        if (crs != null && crs.contains(coordSys)) {
            return true;
        }

        Set<String> srs = this.getSRS();
        if (srs != null && srs.contains(coordSys)) {
            return true;
        }

        return false;
    }

    @Override
    public void setField(QName keyName, Object value) {

        if (keyName.equals(this.crs)) {
            Set<String> crss = (Set<String>) this.getField(this.crs);
            if (crss == null) {
                crss = new HashSet<>();
                super.setField(this.crs, crss);
            }
            crss.add(((XmlModel) value).getCharactersContent());
        } else if (keyName.equals(this.srs)) {
            Set<String> srss = (Set<String>) this.getField(this.srs);
            if (srss == null) {
                srss = new HashSet<>();
                super.setField(this.srs, srss);
            }
            srss.add(((XmlModel) value).getCharactersContent());
        } else if (keyName.equals(this.boundingBox)) {
            Set<WmsBoundingBox> boundingBoxes = (Set<WmsBoundingBox>) this.getField(this.boundingBox);
            if (boundingBoxes == null) {
                boundingBoxes = new HashSet<>();
                super.setField(this.boundingBox, boundingBoxes);
            }
            if (value instanceof WmsBoundingBox) {
                boundingBoxes.add((WmsBoundingBox) value);
            } else {
                super.setField(keyName, value);
            }
        } else if (keyName.equals(this.layer)) {
            List<WmsLayerCapabilities> layers = (List<WmsLayerCapabilities>) this.getField(this.layer);
            if (layers == null) {
                layers = new ArrayList<>();
                super.setField(this.layer, layers);
            }
            if (value instanceof WmsLayerCapabilities) {
                layers.add((WmsLayerCapabilities) value);
            } else {
                super.setField(keyName, value);
            }
        } else if (keyName.equals(this.dataUrl)) {
            Set<WmsLayerInfoUrl> dataUrls = (Set<WmsLayerInfoUrl>) this.getField(this.dataUrl);
            if (dataUrls == null) {
                dataUrls = new HashSet<>();
                super.setField(this.dataUrl, dataUrls);
            }
            if (value instanceof WmsLayerInfoUrl) {
                dataUrls.add((WmsLayerInfoUrl) value);
            } else {
                super.setField(keyName, value);
            }
        } else if (keyName.equals(this.featureListUrl)) {
            Set<WmsLayerInfoUrl> featureUrls = (Set<WmsLayerInfoUrl>) this.getField(this.featureListUrl);
            if (featureUrls == null) {
                featureUrls = new HashSet<>();
                super.setField(this.dataUrl, featureUrls);
            }
            if (value instanceof WmsLayerInfoUrl) {
                featureUrls.add((WmsLayerInfoUrl) value);
            } else {
                super.setField(keyName, value);
            }
        } else if (keyName.equals(this.metadataUrl)) {
            Set<WmsLayerInfoUrl> metadataUrls = (Set<WmsLayerInfoUrl>) this.getField(this.metadataUrl);
            if (metadataUrls == null) {
                metadataUrls = new HashSet<>();
                super.setField(this.metadataUrl, metadataUrls);
            }
            if (value instanceof WmsLayerInfoUrl) {
                metadataUrls.add((WmsLayerInfoUrl) value);
            } else {
                super.setField(keyName, value);
            }
        } else if (keyName.equals(this.identifier)) {
            Set<WmsLayerIdentifier> layerIdentifiers = (Set<WmsLayerIdentifier>) this.getField(this.identifier);
            if (layerIdentifiers == null) {
                layerIdentifiers = new HashSet<>();
                super.setField(this.identifier, layerIdentifiers);
            }
            if (value instanceof WmsLayerIdentifier) {
                layerIdentifiers.add((WmsLayerIdentifier) value);
            } else {
                super.setField(keyName, value);
            }
        } else if (keyName.equals(this.authorityUrl)) {
            Set<WmsAuthorityUrl> authorityUrls = (Set<WmsAuthorityUrl>) this.getField(this.authorityUrl);
            if (authorityUrls == null) {
                authorityUrls = new HashSet<>();
                super.setField(this.authorityUrl, authorityUrls);
            }
            if (value instanceof WmsLayerIdentifier) {
                authorityUrls.add((WmsAuthorityUrl) value);
            } else {
                super.setField(keyName, value);
            }
        } else if (keyName.equals(this.attribution)) {
            Set<WmsLayerAttribution> attributions = (Set<WmsLayerAttribution>) this.getField(this.attribution);
            if (attributions == null) {
                attributions = new HashSet<>();
                super.setField(this.attribution, attributions);
            }
            if (value instanceof WmsLayerAttribution) {
                attributions.add((WmsLayerAttribution) value);
            } else {
                super.setField(keyName, value);
            }
        } else if (keyName.equals(this.extent)) {
            Set<WmsLayerExtent> extents = (Set<WmsLayerExtent>) this.getField(this.extent);
            if (extents == null) {
                extents = new HashSet<>();
                super.setField(this.extent, extents);
            }
            if (value instanceof WmsLayerExtent) {
                extents.add((WmsLayerExtent) value);
            } else {
                super.setField(keyName, value);
            }
        } else if (keyName.equals(this.dimension)) {
            Set<WmsLayerDimension> dimensions = (Set<WmsLayerDimension>) this.getField(this.dimension);
            if (dimensions == null) {
                dimensions = new HashSet<>();
                super.setField(this.dimension, dimensions);
            }
            if (value instanceof WmsLayerDimension) {
                dimensions.add((WmsLayerDimension) value);
            } else {
                super.setField(keyName, value);
            }
        } else if (keyName.equals(this.style)) {
            Set<WmsLayerStyle> styles = (Set<WmsLayerStyle>) this.getField(this.style);
            if (styles == null) {
                styles = new HashSet<>();
                super.setField(this.style, styles);
            }
            if (value instanceof WmsLayerStyle) {
                styles.add((WmsLayerStyle) value);
            } else {
                super.setField(keyName, value);
            }
        } else {
            super.setField(keyName, value);
        }
    }

    @Override
    public String toString() // TODO: Complete this method
    {
        StringBuilder sb = new StringBuilder("LAYER ");

        if (this.getName() != null) {
            sb.append(this.getName()).append(": ");
        }
        sb.append("queryable = ").append(this.isQueryable());

        return sb.toString();
    }
}