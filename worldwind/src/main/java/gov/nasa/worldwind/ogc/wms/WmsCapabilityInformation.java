/*
 * Copyright (c) 2016 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.ogc.wms;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import javax.xml.namespace.QName;

import gov.nasa.worldwind.util.xml.XmlModel;

public class WmsCapabilityInformation extends XmlModel {

    protected QName capabilities;

    protected QName map;

    protected QName feature;

    protected QName exceptions;

    protected QName layers;

    protected QName request;

    public WmsCapabilityInformation(String namespaceUri) {
        super(namespaceUri);
        this.initialize();
    }

    protected void initialize() {
        this.capabilities = new QName(this.getNamespaceUri(), "GetCapabilities");
        this.map = new QName(this.getNamespaceUri(), "GetMap");
        this.feature = new QName(this.getNamespaceUri(), "GetFeatureInfo");
        this.exceptions = new QName(this.getNamespaceUri(), "Exceptions");
        this.layers = new QName(this.getNamespaceUri(), "Layer");
        this.request = new QName(this.getNamespaceUri(), "Request");
    }

    public List<WmsLayerCapabilities> getLayerList() {
        List<WmsLayerCapabilities> layers = (List<WmsLayerCapabilities>) this.getField(this.layers);
        return layers != null ? layers : Collections.<WmsLayerCapabilities>emptyList();
    }

    public Set<String> getImageFormats() {
        XmlModel requests = (XmlModel) this.getField(this.request);
        if (requests == null) {
            return Collections.emptySet();
        }
        WmsRequestDescription mapInfo = (WmsRequestDescription) requests.getField(this.map);
        if (mapInfo == null) {
            return Collections.emptySet();
        }

        return mapInfo.getFormats();
    }

    // TODO add exception list

    public WmsRequestDescription getCapabilitiesInfo() {
        XmlModel request = (XmlModel) this.getField(this.request);
        if (request != null) {
            return (WmsRequestDescription) request.getField(this.capabilities);
        }
        return null;
    }

    public WmsRequestDescription getMapInfo() {
        XmlModel request = (XmlModel) this.getField(this.request);
        if (request != null) {
            return (WmsRequestDescription) request.getField(this.map);
        }
        return null;
    }

    public WmsRequestDescription getFeatureInfo() {
        XmlModel request = (XmlModel) this.getField(this.request);
        if (request != null) {
            return (WmsRequestDescription) request.getField(this.feature);
        }
        return null;
    }

    @Override
    public void setField(QName keyName, Object value) {

        if (keyName.equals(this.layers)) {
            List<WmsLayerCapabilities> layers = (List<WmsLayerCapabilities>) this.getField(this.layers);
            if (layers == null) {
                layers = new ArrayList<>();
                super.setField(this.layers, layers);
            }

            if (value instanceof WmsLayerCapabilities) {
                layers.add((WmsLayerCapabilities) value);
                return;
            }
        }

        super.setField(keyName, value);
    }
}
