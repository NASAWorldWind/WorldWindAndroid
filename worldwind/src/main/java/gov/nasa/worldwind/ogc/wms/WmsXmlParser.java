/*
 * Copyright (c) 2017 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.ogc.wms;

import gov.nasa.worldwind.util.xml.NumberModel;
import gov.nasa.worldwind.util.xml.TextModel;
import gov.nasa.worldwind.util.xml.XmlModelParser;

public class WmsXmlParser extends XmlModelParser {

    public WmsXmlParser() {
        this.registerParsableModels(""); // WMS 1.1.1 namespace
        this.registerParsableModels("http://www.opengis.net/wms"); // WMS 1.3.0 namespace
    }

    protected void registerParsableModels(String namespace) {
        this.registerParsableModel(namespace, "Abstract", TextModel.class);
        this.registerParsableModel(namespace, "AccessConstraints", TextModel.class);
        this.registerParsableModel(namespace, "Address", TextModel.class);
        this.registerParsableModel(namespace, "AddressType", TextModel.class);
        this.registerParsableModel(namespace, "Attribution", WmsLayerAttribution.class);
        this.registerParsableModel(namespace, "AuthorityURL", WmsAuthorityUrl.class);

        this.registerParsableModel(namespace, "BoundingBox", WmsBoundingBox.class);

        this.registerParsableModel(namespace, "Capability", WmsCapabilityInformation.class);
        this.registerParsableModel(namespace, "City", TextModel.class);
        this.registerParsableModel(namespace, "ContactAddress", WmsAddress.class);
        this.registerParsableModel(namespace, "ContactElectronicMailAddress", TextModel.class);
        this.registerParsableModel(namespace, "ContactInformation", WmsContactInformation.class);
        this.registerParsableModel(namespace, "ContactOrganization", TextModel.class);
        this.registerParsableModel(namespace, "ContactPerson", TextModel.class);
        this.registerParsableModel(namespace, "ContactPersonPrimary", WmsContactPersonPrimary.class);
        this.registerParsableModel(namespace, "ContactPosition", TextModel.class);
        this.registerParsableModel(namespace, "ContactVoiceTelephone", TextModel.class);
        this.registerParsableModel(namespace, "Country", TextModel.class);
        this.registerParsableModel(namespace, "CRS", TextModel.class);

        this.registerParsableModel(namespace, "DataURL", WmsLayerInfoUrl.class);
        this.registerParsableModel(namespace, "DCPType", WmsDcpType.class);
        this.registerParsableModel(namespace, "Dimension", WmsLayerDimension.class);

        this.registerParsableModel(namespace, "Extent", WmsLayerDimension.class);
        this.registerParsableModel(namespace, "EX_GeographicBoundingBox", WmsGeographicBoundingBox.class);
        this.registerParsableModel(namespace, "westBoundLongitude", NumberModel.class);
        this.registerParsableModel(namespace, "eastBoundLongitude", NumberModel.class);
        this.registerParsableModel(namespace, "northBoundLatitude", NumberModel.class);
        this.registerParsableModel(namespace, "southBoundLatitude", NumberModel.class);

        this.registerParsableModel(namespace, "Exception", WmsException.class);

        this.registerParsableModel(namespace, "FeatureListURL", WmsLayerInfoUrl.class);
        this.registerParsableModel(namespace, "Fees", TextModel.class);
        this.registerParsableModel(namespace, "Format", WmsFormat.class);

        this.registerParsableModel(namespace, "Get", WmsDcpType.WmsDcpHttpProtocol.class);
        this.registerParsableModel(namespace, "GetCapabilities", WmsRequestOperation.class);
        this.registerParsableModel(namespace, "GetMap", WmsRequestOperation.class);
        this.registerParsableModel(namespace, "GetFeatureInfo", WmsRequestOperation.class);

        this.registerParsableModel(namespace, "HTTP", WmsDcpType.WmsDcpHttp.class);

        this.registerParsableModel(namespace, "Identifier", WmsLayerIdentifier.class);

        this.registerParsableModel(namespace, "Keyword", TextModel.class);
        this.registerParsableModel(namespace, "KeywordList", WmsKeywords.class);

        this.registerParsableModel(namespace, "LatLonBoundingBox", WmsGeographicBoundingBox.class);
        this.registerParsableModel(namespace, "Layer", WmsLayerCapabilities.class);
        this.registerParsableModel(namespace, "LayerInfo", WmsLayerInfoUrl.class);
        this.registerParsableModel(namespace, "LayerLimit", NumberModel.class);
        this.registerParsableModel(namespace, "LegendURL", WmsLogoUrl.class);
        this.registerParsableModel(namespace, "LogoURL", WmsLogoUrl.class);

        this.registerParsableModel(namespace, "MaxHeight", NumberModel.class);
        this.registerParsableModel(namespace, "MaxScaleDenominator", NumberModel.class);
        this.registerParsableModel(namespace, "MaxWidth", NumberModel.class);
        this.registerParsableModel(namespace, "MetadataURL", WmsLayerInfoUrl.class);
        this.registerParsableModel(namespace, "MinScaleDenominator", NumberModel.class);

        this.registerParsableModel(namespace, "Name", TextModel.class);

        this.registerParsableModel(namespace, "OnlineResource", WmsOnlineResource.class);

        this.registerParsableModel(namespace, "Post", WmsDcpType.WmsDcpHttpProtocol.class);
        this.registerParsableModel(namespace, "PostCode", TextModel.class);

        this.registerParsableModel(namespace, "Request", WmsRequestInformation.class);

        this.registerParsableModel(namespace, "ScaleHint", WmsScaleHint.class);
        this.registerParsableModel(namespace, "Service", WmsServiceInformation.class);
        this.registerParsableModel(namespace, "SRS", TextModel.class);
        this.registerParsableModel(namespace, "StateOrProvince", TextModel.class);
        this.registerParsableModel(namespace, "Style", WmsLayerStyle.class);
        this.registerParsableModel(namespace, "StyleSheetURL", WmsLayerInfoUrl.class);
        this.registerParsableModel(namespace, "StyleURL", WmsLayerInfoUrl.class);

        this.registerParsableModel(namespace, "Title", TextModel.class);

        this.registerParsableModel(namespace, "WMS_Capabilities", WmsCapabilities.class);
        this.registerParsableModel(namespace, "WMT_MS_Capabilities", WmsCapabilities.class);
    }
}
