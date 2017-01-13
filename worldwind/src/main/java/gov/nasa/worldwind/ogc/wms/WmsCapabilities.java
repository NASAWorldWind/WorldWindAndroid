/*
 * Copyright (c) 2017 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.ogc.wms;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import gov.nasa.worldwind.util.xml.XmlModel;
import gov.nasa.worldwind.util.xml.XmlPullParserContext;

public class WmsCapabilities extends XmlModel {

    protected String version;

    protected String updateSequence;

    protected WmsCapabilityInformation capabilityInformation;

    protected WmsServiceInformation serviceInformation;

    public WmsCapabilities(String namespaceUri) {
        super(namespaceUri);
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
        List<WmsLayerCapabilities> namedLayers = new ArrayList<>();

        for (WmsLayerCapabilities layer : this.getCapabilityInformation().getLayerList()) {
            namedLayers.addAll(layer.getNamedLayers());
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
        return this.capabilityInformation;
    }

    /**
     * Returns the document's service information.
     *
     * @return the document's service information.
     */
    public WmsServiceInformation getServiceInformation() {
        return this.serviceInformation;
    }

    /**
     * Returns the document's version number.
     *
     * @return the document's version number.
     */
    public String getVersion() {
        return this.version;
    }

    /**
     * Returns the document's update sequence.
     *
     * @return the document's update sequence.
     */
    public String getUpdateSequence() {
        return this.updateSequence;
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
    public void setField(String keyName, Object value) {
        if (keyName.equals("version")) {
            this.version = value.toString();
        } else if (keyName.equals("updateSequence")) {
            this.updateSequence = value.toString();
        } else if (keyName.equals("Service")) {
            this.serviceInformation = (WmsServiceInformation) value;
        } else if (keyName.equals("Capability")) {
            this.capabilityInformation = (WmsCapabilityInformation) value;
        }
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
