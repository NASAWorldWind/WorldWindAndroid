/*
 * Copyright (c) 2016 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.ogc.wms;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.namespace.QName;

import gov.nasa.worldwind.util.xml.XmlModel;
import gov.nasa.worldwind.util.xml.XmlPullParserContext;

public class WmsDcpType extends XmlModel {

    protected QName GET;

    protected QName POST;

    protected QName HTTP;

    protected QName ONLINE_RESOURCE;

    public static class DCPInfo {

        protected String protocol;

        protected String method;

        protected WmsOnlineResource onlineResource;

        public DCPInfo(String protocol) {
            this.protocol = protocol;
        }
    }

    protected List<DCPInfo> protocols = new ArrayList<DCPInfo>(1);

    public WmsDcpType(String namespaceURI) {
        super(namespaceURI);

        this.initialize();
    }

    private void initialize() {
        GET = new QName(this.getNamespaceUri(), "Get");
        POST = new QName(this.getNamespaceUri(), "Post");
        HTTP = new QName(this.getNamespaceUri(), "HTTP");
        ONLINE_RESOURCE = new QName(this.getNamespaceUri(), "OnlineResource");
    }

    @Override
    protected void doParseEventContent(XmlPullParserContext ctx) throws XmlPullParserException, IOException {

        XmlPullParser xpp = ctx.getParser();

        if (xpp.getEventType() == XmlPullParser.START_TAG) {
            if (xpp.getName().equals(HTTP.getLocalPart())) {
                this.addProtocol(xpp.getName());
            } else if (xpp.getName().equals(GET.getLocalPart()) || xpp.getName().equals(POST.getLocalPart())) {
                this.addRequestMethod(xpp.getName());
            } else if (xpp.getName().equals(ONLINE_RESOURCE.getLocalPart())) {
                XmlModel model = ctx.createParsableModel(ONLINE_RESOURCE);
                model.read(ctx);
                if (model != null) {
                    this.addOnlineResource((WmsOnlineResource) model);
                }
            }
        }
    }

    public List<DCPInfo> getDCPInfos() {
        return this.protocols;
    }

    protected void addProtocol(String protocol) {
        this.protocols.add(new DCPInfo(protocol));
    }

    protected void addRequestMethod(String requestMethod) {
        DCPInfo dcpi = this.protocols.get(this.protocols.size() - 1);

        if (dcpi.method != null) {
            dcpi = new DCPInfo(dcpi.protocol);
            this.protocols.add(dcpi);
        }

        dcpi.method = requestMethod;
    }

    protected void addOnlineResource(WmsOnlineResource onlineResource) {
        DCPInfo dcpi = this.protocols.get(this.protocols.size() - 1);

        dcpi.onlineResource = onlineResource;
    }

    public WmsOnlineResource getOnlineResouce(String protocol, String requestMethod) {
        for (DCPInfo dcpi : this.getDCPInfos()) {
            if (!dcpi.protocol.equalsIgnoreCase(protocol))
                continue;

            if (dcpi.method.equalsIgnoreCase(requestMethod))
                return dcpi.onlineResource;
        }

        return null;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        for (DCPInfo dcpi : this.getDCPInfos()) {
            sb.append(dcpi.protocol).append(", ");
            sb.append(dcpi.method).append(", ");
            sb.append(dcpi.onlineResource.toString());
        }

        return sb.toString();
    }
}
