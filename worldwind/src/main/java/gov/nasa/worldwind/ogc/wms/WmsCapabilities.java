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

import gov.nasa.worldwind.util.Logger;
import gov.nasa.worldwind.util.xml.XmlModel;
import gov.nasa.worldwind.util.xml.XmlModelParser;

public class WmsCapabilities extends XmlModel {

    protected String version;

    protected String updateSequence;

    protected WmsService service;

    protected WmsCapability capability;

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
                Logger.logMessage(Logger.ERROR, "WmsCapabilities", "getCapability", "Invalid WMS Capabilities input"));
        }

        return (WmsCapabilities) result;
    }

    /**
     * Returns all named layers in the capabilities document.
     *
     * @return an unordered list of the document's named layers.
     */
    public List<WmsLayer> getNamedLayers() {
        List<WmsLayer> namedLayers = new ArrayList<>();

        for (WmsLayer layer : this.getCapability().getLayers()) {
            namedLayers.addAll(layer.getNamedLayers());
        }

        return namedLayers;
    }

    public WmsLayer getNamedLayer(String name) {
        if (name == null || name.isEmpty()) {
            return null;
        }

        List<WmsLayer> namedLayers = this.getNamedLayers();

        if (namedLayers != null) {
            for (WmsLayer layer : namedLayers) {
                if (layer.getName().equals(name)) {
                    return layer;
                }
            }
        }

        return null;
    }

    public WmsCapability getCapability() {
        return this.capability;
    }

    /**
     * Returns the document's service information.
     *
     * @return the document's service information.
     */
    public WmsService getService() {
        return this.service;
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

    @Override
    public void parseField(String keyName, Object value) {
        if (keyName.equals("version")) {
            this.version = (String) value;
        } else if (keyName.equals("updateSequence")) {
            this.updateSequence = (String) value;
        } else if (keyName.equals("Service")) {
            this.service = (WmsService) value;
        } else if (keyName.equals("Capability")) {
            this.capability = (WmsCapability) value;
        }
    }
}
