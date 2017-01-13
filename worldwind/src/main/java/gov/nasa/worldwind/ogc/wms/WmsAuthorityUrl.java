/*
 * Copyright (c) 2017 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.ogc.wms;

public class WmsAuthorityUrl extends WmsLayerInfoUrl {

    protected String name;

    protected WmsOnlineResource onlineResource;

    public WmsAuthorityUrl(String namespaceURI) {
        super(namespaceURI);
    }

    public String getName() {
        return this.name;
    }

    public WmsOnlineResource getOnlineResource() {
        return this.onlineResource;
    }

    @Override
    public void setField(String keyName, Object value) {
        if (keyName.equals("name")) {
            this.name = value.toString();
        } else if (keyName.equals("OnlineResource")) {
            this.onlineResource = (WmsOnlineResource) value;
        }
    }
}
