/*
 * Copyright (c) 2016 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.ogc.wms;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.xml.namespace.QName;

import gov.nasa.worldwind.util.xml.XmlModel;
import gov.nasa.worldwind.util.xml.XmlPullParserContext;

public class WmsCapabilities extends XmlModel {

    public static final QName VERSION = new QName("", "version");

    public static final QName UPDATE_SEQUENCE = new QName("", "updateSequence");

    protected QName capabilityInformation;

    protected QName serviceInformation;

    public WmsCapabilities(String namespaceUri) {
        super(namespaceUri);
        this.initialize();
    }

    protected void initialize() {
        this.capabilityInformation = new QName(this.getNamespaceUri(), "Capability");
        this.serviceInformation = new QName(this.getNamespaceUri(), "Service");
    }

    public static WmsCapabilities getCapabilities(InputStream is) throws XmlPullParserException, IOException {

        // Initialize the pull parser context
        WmsPullParserContext ctx = new WmsPullParserContext(XmlPullParserContext.DEFAULT_NAMESPACE);
        ctx.setParserInput(is);

        // Parse the Xml document until a Wms service is discovered
        WmsCapabilities wmsCapabilities = new WmsCapabilities(XmlPullParserContext.DEFAULT_NAMESPACE);

        wmsCapabilities.read(ctx);

        return wmsCapabilities;
    }

    /**
     * Returns all named layers in the capabilities document.
     *
     * @return an unordered list of the document's named layers.
     */
    public List<WmsLayerCapabilities> getNamedLayers() {

        WmsCapabilityInformation capInfo = (WmsCapabilityInformation) this.getField(this.capabilityInformation);

        if (capInfo == null) {
            return null;
        }

        List<WmsLayerCapabilities> namedLayers = new ArrayList<>();

        for (WmsLayerCapabilities topLevelLayer : capInfo.getLayerList()) {
            if (topLevelLayer.getName() != null && !topLevelLayer.getName().isEmpty()) {
                namedLayers.add(topLevelLayer);
            }
            List<WmsLayerCapabilities> named = topLevelLayer.getNamedLayers();
            for (WmsLayerCapabilities layer : named) {
                namedLayers.addAll(layer.getNamedLayers());
            }
        }

        return namedLayers;
    }

    public WmsLayerCapabilities getLayerByName(String name) {

        if (name == null || name.isEmpty()) {
            return null;
        }

        List<WmsLayerCapabilities> namedLayers = this.getNamedLayers();

        if (namedLayers != null) {
            for (WmsLayerCapabilities layer : namedLayers) {
                if (layer.getName().equals(name)) {
                    return layer;
                }
            }
        }

        return null;
    }

    public WmsCapabilityInformation getCapabilityInformation() {
        return (WmsCapabilityInformation) this.getField(this.capabilityInformation);
    }

    /**
     * Returns the document's service information.
     *
     * @return the document's service information.
     */
    public WmsServiceInformation getServiceInformation() {
        return (WmsServiceInformation) this.getField(this.serviceInformation);
    }

    /**
     * Returns the document's version number.
     *
     * @return the document's version number.
     */
    public String getVersion() {
        return this.getField(VERSION).toString();
    }

    /**
     * Returns the document's update sequence.
     *
     * @return the document's update sequence.
     */
    public String getUpdateSequence() {
        Object o = this.getField(UPDATE_SEQUENCE);
        return o != null ? o.toString() : null;
    }

    public Set<String> getImageFormats() {
        WmsCapabilityInformation capInfo = this.getCapabilityInformation();
        if (capInfo == null) {
            return null;
        }

        return capInfo.getImageFormats();
    }

    public String getRequestURL(String requestName, String requestMethod) {

        if (requestName == null || requestMethod == null) {
            return null;
        }

        WmsCapabilityInformation capabilityInformation = this.getCapabilityInformation();
        if (capabilityInformation == null) {
            return null;
        }

        WmsRequestDescription requestDescription = null;
        if (requestName.equals("GetCapabilities")) {
            requestDescription = capabilityInformation.getCapabilitiesInfo();
        } else if (requestName.equals("GetMap")) {
            requestDescription = capabilityInformation.getMapInfo();
        } else if (requestName.equals("GetFeatureInfo")) {
            requestDescription = capabilityInformation.getFeatureInfo();
        }

        if (requestDescription == null) {
            return null;
        }

        WmsOnlineResource onlineResource = requestDescription.getOnlineResource(requestMethod);
        if (onlineResource == null) {
            return null;
        }

        return onlineResource.getHref();
    }

    @Override
    public String toString() // TODO: Complete this method
    {
        StringBuilder sb = new StringBuilder();

        sb.append("Version: ").
            append(this.getVersion() != null ? this.getVersion() : "none").append("\n");
        sb.append("UpdateSequence: ").
            append(this.getUpdateSequence() != null ? this.getUpdateSequence() : "none");
        sb.append("\n");
        sb.append(this.getServiceInformation() != null ? this.getServiceInformation() : "Service Information: none");
        sb.append("\n");
        sb.append(this.getCapabilityInformation() != null
            ? this.getCapabilityInformation() : "Capability Information: none");
        sb.append("\n");

        sb.append("LAYERS\n");

        for (WmsLayerCapabilities layerCaps : this.getNamedLayers()) {
            sb.append(layerCaps.toString()).append("\n");
        }

        return sb.toString();
    }
}
