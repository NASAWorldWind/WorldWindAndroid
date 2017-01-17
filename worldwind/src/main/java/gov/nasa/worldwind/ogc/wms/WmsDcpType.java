/*
 * Copyright (c) 2017 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.ogc.wms;

import java.util.ArrayList;
import java.util.List;

import gov.nasa.worldwind.util.xml.XmlModel;

public class WmsDcpType extends XmlModel {

    protected WmsOnlineResource get;

    protected WmsOnlineResource post;

    public static class DcpInfo {

        protected String protocol;

        protected String method;

        protected WmsOnlineResource onlineResource;

        public DcpInfo(String protocol) {
            this.protocol = protocol;
        }
    }

    public WmsDcpType(String namespaceURI) {
        super(namespaceURI);
    }

    public List<DcpInfo> getDcpInfos() {

        List<DcpInfo> infos = new ArrayList<>();

        if (get != null) {
            DcpInfo dcpInfo = new DcpInfo("HTTP");
            dcpInfo.method = "Get";
            dcpInfo.onlineResource = this.get;
            infos.add(dcpInfo);
        }

        if (post != null) {
            DcpInfo dcpInfo = new DcpInfo("HTTP");
            dcpInfo.method = "Post";
            dcpInfo.onlineResource = this.post;
            infos.add(dcpInfo);
        }

        return infos;
    }

    @Override
    public void setField(String keyName, Object value) {
        if (keyName.equals("HTTP")) {
            WmsDcpHttp http = (WmsDcpHttp) value;
            this.get = http.get != null ? http.get.onlineResource : null;
            this.post = http.post != null ? http.post.onlineResource : null;
        }
    }

    protected static class WmsDcpHttp extends XmlModel {

        protected WmsDcpHttpProtocol get;

        protected WmsDcpHttpProtocol post;

        public WmsDcpHttp(String namespaceUri) {
            super(namespaceUri);
        }

        @Override
        public void setField(String keyName, Object value) {
            if (keyName.equals("Get")) {
                this.get = (WmsDcpHttpProtocol) value;
            } else if (keyName.equals("Post")) {
                this.post = (WmsDcpHttpProtocol) value;
            }
        }
    }

    protected static class WmsDcpHttpProtocol extends XmlModel {

        protected WmsOnlineResource onlineResource;

        public WmsDcpHttpProtocol(String namespaceUri) {
            super(namespaceUri);
        }

        @Override
        public void setField(String keyName, Object value) {
            if (keyName.equals("OnlineResource")) {
                this.onlineResource = (WmsOnlineResource) value;
            }
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        for (DcpInfo dcpi : this.getDcpInfos()) {
            sb.append(dcpi.protocol).append(", ");
            sb.append(dcpi.method).append(", ");
            sb.append(dcpi.onlineResource.toString());
        }

        return sb.toString();
    }
}
