/*
 * Copyright (c) 2017 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.ogc.wms;

import android.util.Xml;

import org.xmlpull.v1.XmlPullParser;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import gov.nasa.worldwind.util.Logger;
import gov.nasa.worldwind.util.xml.XmlModel;
import gov.nasa.worldwind.util.xml.XmlModelParser;

public class WmsCapabilities extends XmlModel {

    protected String version;

    protected String updateSequence;

    protected WmsCapabilityInformation capabilityInformation;

    protected WmsServiceInformation serviceInformation;

    public WmsCapabilities() {
    }

    public static WmsCapabilities getCapabilities(InputStream inputStream) throws Exception {
        XmlPullParser pullParser = Xml.newPullParser();
        pullParser.setInput(inputStream, null /*inputEncoding*/);

        XmlModelParser modelParser = new WmsXmlParser();
        modelParser.setPullParser(pullParser);

        Object result = modelParser.parse();
        if (!(result instanceof WmsCapabilities)) {
            throw new RuntimeException(
                Logger.logMessage(Logger.ERROR, "WmsCapabilities", "getCapabilities", "Invalid WMS Capabilities input"));
        }

        return (WmsCapabilities) result;
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

        WmsRequestOperation requestDescription = null;
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
    public void parseField(String keyName, Object value) {
        if (keyName.equals("version")) {
            this.version = (String) value;
        } else if (keyName.equals("updateSequence")) {
            this.updateSequence = (String) value;
        } else if (keyName.equals("Service")) {
            this.serviceInformation = (WmsServiceInformation) value;
        } else if (keyName.equals("Capability")) {
            this.capabilityInformation = (WmsCapabilityInformation) value;
        }
    }
}
