/*
 * Copyright (c) 2017 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.ogc.wms;

import gov.nasa.worldwind.util.xml.XmlModel;

public class WmsDcpType extends XmlModel {

    protected WmsOnlineResource get;

    protected WmsOnlineResource post;

    public String getGetHref() {
        return (this.get != null) ? this.get.getUrl() : null;
    }

    public String getPostHref() {
        return (this.post != null) ? this.post.getUrl() : null;
    }

    @Override
    public void parseField(String keyName, Object value) {
        if (keyName.equals("HTTP")) {
            WmsDcpHttp http = (WmsDcpHttp) value;
            this.get = http.get != null ? http.get.onlineResource : null;
            this.post = http.post != null ? http.post.onlineResource : null;
        }
    }

    protected static class WmsDcpHttp extends XmlModel {

        protected WmsDcpHttpProtocol get;

        protected WmsDcpHttpProtocol post;

        public WmsDcpHttp() {
        }

        @Override
        public void parseField(String keyName, Object value) {
            if (keyName.equals("Get")) {
                this.get = (WmsDcpHttpProtocol) value;
            } else if (keyName.equals("Post")) {
                this.post = (WmsDcpHttpProtocol) value;
            }
        }
    }

    protected static class WmsDcpHttpProtocol extends XmlModel {

        protected WmsOnlineResource onlineResource;

        public WmsDcpHttpProtocol() {
        }

        @Override
        public void parseField(String keyName, Object value) {
            if (keyName.equals("OnlineResource")) {
                this.onlineResource = (WmsOnlineResource) value;
            }
        }
    }
}
