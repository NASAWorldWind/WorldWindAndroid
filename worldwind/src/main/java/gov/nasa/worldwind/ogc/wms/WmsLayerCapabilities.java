/*
 * Copyright (c) 2017 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.ogc.wms;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.util.Logger;
import gov.nasa.worldwind.util.xml.XmlModel;

public class WmsLayerCapabilities extends XmlModel {

    // Properties of the Layer element
    protected List<WmsLayerCapabilities> layers = new ArrayList<>();

    protected String name;

    protected String title;

    protected String description;

    protected WmsKeywords keywords;

    protected Set<WmsLayerStyle> styles = new LinkedHashSet<>();

    // The 1.3.0 Reference System
    protected Set<String> availableCrs = new LinkedHashSet<>();

    // The 1.1.1 Reference System
    protected Set<String> availableSrs = new LinkedHashSet<>();

    protected WmsGeographicBoundingBox geographicBoundingBox;

    protected Set<WmsBoundingBox> boundingBoxes = new LinkedHashSet<>();

    // The 1.3.0 Dimension Property
    protected Set<WmsLayerDimension> dimensions = new LinkedHashSet<>();

    // The 1.1.1 Dimension Property
    protected Set<WmsLayerDimension> extents = new LinkedHashSet<>();

    protected WmsLayerAttribution attribution;

    protected Set<WmsAuthorityUrl> authorityUrls = new LinkedHashSet<>();

    protected Set<WmsLayerIdentifier> identifiers = new LinkedHashSet<>();

    protected Set<WmsLayerInfoUrl> metadataUrls = new LinkedHashSet<>();

    protected Set<WmsLayerInfoUrl> dataUrls = new LinkedHashSet<>();

    protected Set<WmsLayerInfoUrl> featureListUrls = new LinkedHashSet<>();

    // The 1.3.0 Scale Property
    protected Double maxScaleDenominator;

    // The 1.3.0 Scale Property
    protected Double minScaleDenominator;

    // The 1.1.1 Scale Property
    protected Double maxScaleHint;

    // The 1.1.1 Scale Property
    protected Double minScaleHint;

    // Properties of the Layer attributes
    protected boolean queryable;

    protected Integer cascaded;

    protected Boolean opaque;

    protected Boolean noSubsets;

    protected Integer fixedWidth;

    protected Integer fixedHeight;

    public WmsLayerCapabilities() {
    }

    public List<WmsLayerCapabilities> getNamedLayers() {
        List<WmsLayerCapabilities> namedLayers = new ArrayList<>();

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

    public Double getMinScaleHint() {
        Double actualMinScaleHint = this.minScaleHint;

        XmlModel parent = this.getParent();

        while (actualMinScaleHint == null && parent != null) {
            if (parent instanceof WmsLayerCapabilities) {
                actualMinScaleHint = ((WmsLayerCapabilities) parent).minScaleHint;
            }
            parent = parent.getParent();
        }

        return actualMinScaleHint;
    }

    public Double getMaxScaleHint() {
        Double actualMaxScaleHint = this.maxScaleHint;

        XmlModel parent = this.getParent();

        while (actualMaxScaleHint == null && parent != null) {
            if (parent instanceof WmsLayerCapabilities) {
                actualMaxScaleHint = ((WmsLayerCapabilities) parent).maxScaleHint;
            }
            parent = parent.getParent();
        }

        return actualMaxScaleHint;
    }

    public Set<WmsLayerDimension> getDimensions() {
        XmlModel parent = this.getParent();

        while (parent != null) {
            if (parent instanceof WmsLayerCapabilities) {
                this.dimensions.addAll(((WmsLayerCapabilities) parent).dimensions);
            }
            parent = parent.getParent();
        }

        return Collections.unmodifiableSet(this.dimensions);
    }

    public Set<WmsLayerDimension> getExtents() {
        XmlModel parent = this.getParent();

        while (parent != null) {
            if (parent instanceof WmsLayerCapabilities) {
                this.extents.addAll(((WmsLayerCapabilities) parent).extents);
            }
            parent = parent.getParent();
        }

        return Collections.unmodifiableSet(this.extents);
    }

    public Integer getCascaded() {
        Integer actualCascade = this.cascaded;

        XmlModel parent = this.getParent();

        while (actualCascade == null && parent != null) {
            if (parent instanceof WmsLayerCapabilities) {
                actualCascade = ((WmsLayerCapabilities) parent).cascaded;
            }
            parent = parent.getParent();
        }

        return actualCascade;
    }

    public Integer getFixedHeight() {
        Integer actualFixedHeight = this.fixedHeight;

        XmlModel parent = this.getParent();

        while (actualFixedHeight == null && parent != null) {
            if (parent instanceof WmsLayerCapabilities) {
                actualFixedHeight = ((WmsLayerCapabilities) parent).fixedHeight;
            }
            parent = parent.getParent();
        }

        return actualFixedHeight;
    }

    public Integer getFixedWidth() {
        Integer actualFixedWidth = this.fixedWidth;

        XmlModel parent = this.getParent();

        while (actualFixedWidth == null && parent != null) {
            if (parent instanceof WmsLayerCapabilities) {
                actualFixedWidth = ((WmsLayerCapabilities) parent).fixedWidth;
            }
            parent = parent.getParent();
        }

        return actualFixedWidth;
    }

    public Boolean isNoSubsets() {
        Boolean actualNoSubsets = this.noSubsets;

        XmlModel parent = this.getParent();

        while (actualNoSubsets == null && parent != null) {
            if (parent instanceof WmsLayerCapabilities) {
                actualNoSubsets = ((WmsLayerCapabilities) parent).noSubsets;
            }
            parent = parent.getParent();
        }

        return actualNoSubsets;
    }

    public Boolean isOpaque() {
        Boolean actualOpaque = this.opaque;

        XmlModel parent = this.getParent();

        while (actualOpaque == null && parent != null) {
            if (parent instanceof WmsLayerCapabilities) {
                actualOpaque = ((WmsLayerCapabilities) parent).opaque;
            }
            parent = parent.getParent();
        }

        return actualOpaque;
    }

    public boolean isQueryable() {
        Boolean actualQueryable = this.queryable;

        XmlModel parent = this.getParent();

        while (actualQueryable == null && parent != null) {
            if (parent instanceof WmsLayerCapabilities) {
                actualQueryable = ((WmsLayerCapabilities) parent).opaque;
            }
            parent = parent.getParent();
        }

        return actualQueryable;
    }

    public WmsLayerAttribution getAttribution() {
        WmsLayerAttribution actualAttribution = this.attribution;

        XmlModel parent = this.getParent();

        while (actualAttribution == null && parent != null) {
            if (parent instanceof WmsLayerCapabilities) {
                actualAttribution = ((WmsLayerCapabilities) parent).attribution;
            }
            parent = parent.getParent();
        }

        return actualAttribution;
    }

    public Set<WmsAuthorityUrl> getAuthorityUrls() {
        XmlModel parent = this.getParent();

        while (parent != null) {
            if (parent instanceof WmsLayerCapabilities) {
                this.authorityUrls.addAll(((WmsLayerCapabilities) parent).authorityUrls);
                break;
            }
            parent = parent.getParent();
        }

        return Collections.unmodifiableSet(this.authorityUrls);
    }

    public Set<WmsLayerIdentifier> getIdentifiers() {
        return Collections.unmodifiableSet(this.identifiers);
    }

    public Set<WmsLayerInfoUrl> getMetadataUrls() {
        return Collections.unmodifiableSet(this.metadataUrls);
    }

    public Set<WmsLayerInfoUrl> getFeatureListUrls() {
        return Collections.unmodifiableSet(this.featureListUrls);
    }

    public Set<WmsLayerInfoUrl> getDataUrls() {
        return Collections.unmodifiableSet(this.dataUrls);
    }

    public List<WmsLayerCapabilities> getLayers() {
        return Collections.unmodifiableList(this.layers);
    }

    public Set<WmsLayerStyle> getStyles() {
        XmlModel parent = this.getParent();

        while (parent != null) {
            if (parent instanceof WmsLayerCapabilities) {
                this.styles.addAll(((WmsLayerCapabilities) parent).styles);
                break;
            }
            parent = parent.getParent();
        }

        return Collections.unmodifiableSet(this.styles);
    }

    public Set<WmsBoundingBox> getBoundingBoxes() {
        XmlModel parent = this.getParent();

        while (parent != null) {
            if (parent instanceof WmsLayerCapabilities) {
                this.boundingBoxes.addAll(((WmsLayerCapabilities) parent).boundingBoxes);
                break;
            }
            parent = parent.getParent();
        }

        return Collections.unmodifiableSet(this.boundingBoxes);
    }

    public Sector getGeographicBoundingBox() {
        WmsGeographicBoundingBox actualGeographicBoundingBox = this.geographicBoundingBox;

        XmlModel parent = this.getParent();

        while (actualGeographicBoundingBox == null && parent != null) {
            if (parent instanceof WmsLayerCapabilities) {
                actualGeographicBoundingBox = ((WmsLayerCapabilities) parent).geographicBoundingBox;
            }
            parent = parent.getParent();
        }

        if (actualGeographicBoundingBox != null) {
            return actualGeographicBoundingBox.getGeographicBoundingBox();
        }
        return null;
    }

    public Set<String> getKeywords() {
        return this.keywords.getKeywords();
    }

    public String getLayerAbstract() {
        return this.description;
    }

    public Double getMaxScaleDenominator() {
        Double actualMaxScaleDenominator = this.maxScaleDenominator;

        XmlModel parent = this.getParent();

        while (actualMaxScaleDenominator == null && parent != null) {
            if (parent instanceof WmsLayerCapabilities) {
                actualMaxScaleDenominator = ((WmsLayerCapabilities) parent).maxScaleDenominator;
            }
            parent = parent.getParent();
        }

        return actualMaxScaleDenominator;
    }

    public Double getMinScaleDenominator() {
        Double actualMinScaleDenominator = this.minScaleDenominator;

        XmlModel parent = this.getParent();

        while (actualMinScaleDenominator == null && parent != null) {
            if (parent instanceof WmsLayerCapabilities) {
                actualMinScaleDenominator = ((WmsLayerCapabilities) parent).minScaleDenominator;
            }
            parent = parent.getParent();
        }

        return actualMinScaleDenominator;
    }

    public String getName() {
        return this.name;
    }

    public String getTitle() {
        return this.title;
    }

    public Set<String> getSrs() {
        XmlModel parent = this.getParent();

        while (parent != null) {
            if (parent instanceof WmsLayerCapabilities) {
                this.availableSrs.addAll(((WmsLayerCapabilities) parent).availableSrs);
            }
            parent = parent.getParent();
        }

        return Collections.unmodifiableSet(this.availableSrs);
    }

    public Set<String> getCrs() {
        XmlModel parent = this.getParent();

        while (parent != null) {
            if (parent instanceof WmsLayerCapabilities) {
                this.availableCrs.addAll(((WmsLayerCapabilities) parent).availableCrs);
            }
            parent = parent.getParent();
        }

        return Collections.unmodifiableSet(this.availableCrs);
    }

    /**
     * Provides a WMS version agnostic reference system by returning the non-null reference system set. It is the
     * responsibility of the caller to understand which WMS version is being used.
     *
     * @return a set of reference systems supported by this layer
     */
    public Set<String> getReferenceSystem() {
        Set<String> rs = this.getCrs();

        if (rs == null || rs.size() == 0) {
            rs = this.getSrs();
        }

        return rs;
    }

    public boolean hasCoordinateSystem(String coordSys) {
        if (coordSys == null)
            return false;

        Set<String> crs = this.getCrs();
        if (crs != null && crs.contains(coordSys)) {
            return true;
        }

        Set<String> srs = this.getSrs();
        return srs != null && srs.contains(coordSys);
    }

    public WmsCapabilities getServiceCapabilities() {
        XmlModel model = this;

        while (model != null) {
            model = model.getParent();
            if (model instanceof WmsCapabilities) {
                return (WmsCapabilities) model;
            }
        }

        return null;
    }

    @Override
    public void parseField(String keyName, Object value) {
        if (keyName.equals("Layer")) {
            this.layers.add((WmsLayerCapabilities) value);
        } else if (keyName.equals("Name")) {
            this.name = (String) value;
        } else if (keyName.equals("Title")) {
            this.title = (String) value;
        } else if (keyName.equals("Abstract")) {
            this.description = (String) value;
        } else if (keyName.equals("KeywordList")) {
            this.keywords = (WmsKeywords) value;
        } else if (keyName.equals("Style")) {
            this.styles.add((WmsLayerStyle) value);
        } else if (keyName.equals("CRS")) {
            this.availableCrs.add((String) value);
        } else if (keyName.equals("SRS")) {
            this.availableSrs.add((String) value);
        } else if (keyName.equals("EX_GeographicBoundingBox")) {
            this.geographicBoundingBox = (WmsGeographicBoundingBox) value;
        } else if (keyName.equals("LatLonBoundingBox")) {
            this.geographicBoundingBox = (WmsGeographicBoundingBox) value;
        } else if (keyName.equals("BoundingBox")) {
            this.boundingBoxes.add((WmsBoundingBox) value);
        } else if (keyName.equals("Dimension")) {
            this.dimensions.add((WmsLayerDimension) value);
        } else if (keyName.equals("Extent")) {
            this.extents.add((WmsLayerDimension) value);
        } else if (keyName.equals("Attribution")) {
            this.attribution = (WmsLayerAttribution) value;
        } else if (keyName.equals("AuthorityURL")) {
            this.authorityUrls.add((WmsAuthorityUrl) value);
        } else if (keyName.equals("Identifier")) {
            this.identifiers.add((WmsLayerIdentifier) value);
        } else if (keyName.equals("MetadataURL")) {
            this.metadataUrls.add((WmsLayerInfoUrl) value);
        } else if (keyName.equals("DataURL")) {
            this.dataUrls.add((WmsLayerInfoUrl) value);
        } else if (keyName.equals("FeatureListURL")) {
            this.featureListUrls.add((WmsLayerInfoUrl) value);
        } else if (keyName.equals("MinScaleDenominator")) {
            this.minScaleDenominator = Double.parseDouble((String) value);
        } else if (keyName.equals("MaxScaleDenominator")) {
            this.maxScaleDenominator = Double.parseDouble((String) value);
        } else if (keyName.equals("ScaleHint")) {
            WmsScaleHint scaleHint = (WmsScaleHint) value;
            this.minScaleHint = scaleHint.min;
            this.maxScaleHint = scaleHint.max;
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