/*
 * Copyright (c) 2016 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.ogc.wms;

import java.util.ArrayList;
import java.util.List;

import javax.xml.namespace.QName;

import gov.nasa.worldwind.util.xml.NameStringModel;
import gov.nasa.worldwind.util.xml.XmlModel;

public class WmsDcpType extends XmlModel {

    protected QName get;

    protected QName post;

    protected QName http;

    protected QName onlineResource;

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

        this.initialize();
    }

    private void initialize() {
        this.get = new QName(this.getNamespaceUri(), "Get");
        this.post = new QName(this.getNamespaceUri(), "Post");
        this.http = new QName(this.getNamespaceUri(), "HTTP");
        this.onlineResource = new QName(this.getNamespaceUri(), "OnlineResource");
    }

    public List<DcpInfo> getDcpInfos() {

        List<DcpInfo> infos = new ArrayList<>();

        NameStringModel httpModel = (NameStringModel) this.getField(this.http);
        if (httpModel != null) {

            NameStringModel model = (NameStringModel) httpModel.getField(this.get);
            if (model != null) {
                DcpInfo dcpInfo = new DcpInfo(httpModel.getField(XmlModel.CHARACTERS_CONTENT).toString());
                dcpInfo.method = model.getField(XmlModel.CHARACTERS_CONTENT).toString();
                dcpInfo.onlineResource = (WmsOnlineResource) model.getField(this.onlineResource);
                infos.add(dcpInfo);
            }

            model = (NameStringModel) httpModel.getField(this.post);
            if (model != null) {
                DcpInfo dcpInfo = new DcpInfo(httpModel.getField(XmlModel.CHARACTERS_CONTENT).toString());
                dcpInfo.method = model.getField(XmlModel.CHARACTERS_CONTENT).toString();
                dcpInfo.onlineResource = (WmsOnlineResource) model.getField(this.onlineResource);
                infos.add(dcpInfo);
            }
        }

        return infos;
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
