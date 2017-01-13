/*
 * Copyright (c) 2017 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.ogc.wms;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import gov.nasa.worldwind.util.xml.XmlModel;

public class WmsCapabilityInformation extends XmlModel {

    protected WmsRequestDescription capabilities;

    protected WmsRequestDescription map;

    protected WmsRequestDescription feature;

    protected List<WmsLayerCapabilities> layers = new ArrayList<>();

    protected Set<String> exceptions = new LinkedHashSet<>();

    public WmsCapabilityInformation(String namespaceUri) {
        super(namespaceUri);
    }

    public List<WmsLayerCapabilities> getLayerList() {
        return Collections.unmodifiableList(this.layers);
    }

    public Set<String> getImageFormats() {
        return this.map.getFormats();
    }

    public WmsRequestDescription getCapabilitiesInfo() {
        return this.capabilities;
    }

    public WmsRequestDescription getMapInfo() {
        return this.map;
    }

    public WmsRequestDescription getFeatureInfo() {
        return this.feature;
    }

    @Override
    public void setField(String keyName, Object value) {
        // GetCapabilities, GetMap, and GetFeatureInfo are located in a Request element. We don't want to expose an
        // additional Element parser so this a method to "skip" the Request element in order to access the desired fields
        if (keyName.equals("Request")) {
            Map<String, Object> requests = ((XmlModel) value).getFields();
            for (Map.Entry<String, Object> request : requests.entrySet()) {
                this.setField(request.getKey(), request.getValue());
            }
            return;
        }

        if (keyName.equals("GetCapabilities")) {
            this.capabilities = (WmsRequestDescription) value;
        } else if (keyName.equals("GetMap")) {
            this.map = (WmsRequestDescription) value;
        } else if (keyName.equals("GetFeatureInfo")) {
            this.feature = (WmsRequestDescription) value;
        } else if (keyName.equals("Exception")) {
            this.exceptions.addAll(((WmsException) value).getExceptionFormats());
        } else if (keyName.equals("Layer")) {
            this.layers.add((WmsLayerCapabilities) value);
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        sb.append("Image Formats:\n");
        for (String imageFormat : this.getImageFormats()) {
            sb.append(imageFormat).append("\n");
        }
        sb.append("Capabilities Info: " + this.getCapabilitiesInfo()).append("\n");
        sb.append("Map Info: ").append(this.getMapInfo()).append("\n");
        sb.append("Feature Info: ").append(this.getFeatureInfo() != null ? this.getFeatureInfo() : "none").append("\n");

        return sb.toString();
    }
}
