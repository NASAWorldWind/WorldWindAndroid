/*
 * Copyright (c) 2017 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.ogc.wms;

import java.util.ArrayList;
import java.util.List;

import gov.nasa.worldwind.util.xml.XmlModel;

public class WmsCapability extends XmlModel {

    protected WmsRequest request;

    protected List<WmsLayer> layers = new ArrayList<>();

    /**
     * Object representation of an Exception element. Pre-allocated to prevent NPE in the event the server does not
     * include an Exception block.
     */
    protected WmsException exception = new WmsException();

    public WmsCapability() {
    }

    public List<WmsLayer> getLayers() {
        return this.layers;
    }

    public WmsRequest getRequest() {
        return this.request;
    }

    public WmsException getException() {
        return this.exception;
    }

    public WmsCapabilities getCapabilities() {
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
        if (keyName.equals("Request")) {
            this.request = (WmsRequest) value;
        } else if (keyName.equals("Exception")) {
            this.exception.formats.addAll(((WmsException) value).getFormats());
        } else if (keyName.equals("Layer")) {
            this.layers.add((WmsLayer) value);
        }
    }
}
