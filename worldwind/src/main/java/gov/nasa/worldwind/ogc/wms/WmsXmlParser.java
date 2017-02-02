/*
 * Copyright (c) 2017 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.ogc.wms;

import gov.nasa.worldwind.util.xml.XmlModelParser;

public class WmsXmlParser extends XmlModelParser {

    public WmsXmlParser() {
        this.registerNamespace(""); // WMS 1.1.1 namespace
        this.registerNamespace("http://www.opengis.net/wms"); // WMS 1.3.0 namespace
    }

    protected void registerNamespace(String namespace) {
        this.registerTxtModel(namespace, "Abstract");
        this.registerTxtModel(namespace, "AccessConstraints");
        this.registerTxtModel(namespace, "Address");
        this.registerTxtModel(namespace, "AddressType");
        this.registerXmlModel(namespace, "Attribution", WmsAttribution.class);
        this.registerXmlModel(namespace, "AuthorityURL", WmsAuthorityUrl.class);

        this.registerXmlModel(namespace, "BoundingBox", WmsBoundingBox.class);

        this.registerXmlModel(namespace, "Capability", WmsCapability.class);
        this.registerTxtModel(namespace, "City");
        this.registerXmlModel(namespace, "ContactAddress", WmsAddress.class);
        this.registerTxtModel(namespace, "ContactElectronicMailAddress");
        this.registerXmlModel(namespace, "ContactInformation", WmsContactInformation.class);
        this.registerTxtModel(namespace, "ContactOrganization");
        this.registerTxtModel(namespace, "ContactPerson");
        this.registerXmlModel(namespace, "ContactPersonPrimary", WmsContactPersonPrimary.class);
        this.registerTxtModel(namespace, "ContactPosition");
        this.registerTxtModel(namespace, "ContactVoiceTelephone");
        this.registerTxtModel(namespace, "Country");
        this.registerTxtModel(namespace, "CRS");

        this.registerXmlModel(namespace, "DataURL", WmsInfoUrl.class);
        this.registerXmlModel(namespace, "DCPType", WmsDcpType.class);
        this.registerXmlModel(namespace, "Dimension", WmsDimension.class);

        this.registerXmlModel(namespace, "Extent", WmsDimension.class);
        this.registerXmlModel(namespace, "EX_GeographicBoundingBox", WmsGeographicBoundingBox.class);
        this.registerTxtModel(namespace, "westBoundLongitude");
        this.registerTxtModel(namespace, "eastBoundLongitude");
        this.registerTxtModel(namespace, "northBoundLatitude");
        this.registerTxtModel(namespace, "southBoundLatitude");

        this.registerXmlModel(namespace, "Exception", WmsException.class);

        this.registerXmlModel(namespace, "FeatureListURL", WmsInfoUrl.class);
        this.registerTxtModel(namespace, "Fees");
        this.registerTxtModel(namespace, "Format");

        this.registerXmlModel(namespace, "Get", WmsDcpType.WmsDcpHttpProtocol.class);
        this.registerXmlModel(namespace, "GetCapabilities", WmsRequestOperation.class);
        this.registerXmlModel(namespace, "GetMap", WmsRequestOperation.class);
        this.registerXmlModel(namespace, "GetFeatureInfo", WmsRequestOperation.class);

        this.registerXmlModel(namespace, "HTTP", WmsDcpType.WmsDcpHttp.class);

        this.registerXmlModel(namespace, "Identifier", WmsIdentifier.class);

        this.registerTxtModel(namespace, "Keyword");
        this.registerXmlModel(namespace, "KeywordList", WmsKeywords.class);

        this.registerXmlModel(namespace, "LatLonBoundingBox", WmsGeographicBoundingBox.class);
        this.registerXmlModel(namespace, "Layer", WmsLayer.class);
        this.registerXmlModel(namespace, "LayerInfo", WmsInfoUrl.class);
        this.registerTxtModel(namespace, "LayerLimit");
        this.registerXmlModel(namespace, "LegendURL", WmsLogoUrl.class);
        this.registerXmlModel(namespace, "LogoURL", WmsLogoUrl.class);

        this.registerTxtModel(namespace, "MaxHeight");
        this.registerTxtModel(namespace, "MaxScaleDenominator");
        this.registerTxtModel(namespace, "MaxWidth");
        this.registerXmlModel(namespace, "MetadataURL", WmsInfoUrl.class);
        this.registerTxtModel(namespace, "MinScaleDenominator");

        this.registerTxtModel(namespace, "Name");

        this.registerXmlModel(namespace, "OnlineResource", WmsOnlineResource.class);

        this.registerXmlModel(namespace, "Post", WmsDcpType.WmsDcpHttpProtocol.class);
        this.registerTxtModel(namespace, "PostCode");

        this.registerXmlModel(namespace, "Request", WmsRequest.class);

        this.registerXmlModel(namespace, "ScaleHint", WmsScaleHint.class);
        this.registerXmlModel(namespace, "Service", WmsService.class);
        this.registerTxtModel(namespace, "SRS");
        this.registerTxtModel(namespace, "StateOrProvince");
        this.registerXmlModel(namespace, "Style", WmsStyle.class);
        this.registerXmlModel(namespace, "StyleSheetURL", WmsInfoUrl.class);
        this.registerXmlModel(namespace, "StyleURL", WmsInfoUrl.class);

        this.registerTxtModel(namespace, "Title");

        this.registerXmlModel(namespace, "WMS_Capabilities", WmsCapabilities.class);
        this.registerXmlModel(namespace, "WMT_MS_Capabilities", WmsCapabilities.class);
    }
}
