/*
 * Copyright (c) 2017 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.ogc.wms;

import javax.xml.namespace.QName;

import gov.nasa.worldwind.util.xml.DoubleModel;
import gov.nasa.worldwind.util.xml.IntegerModel;
import gov.nasa.worldwind.util.xml.TextModel;
import gov.nasa.worldwind.util.xml.XmlPullParserContext;

public class WmsPullParserContext extends XmlPullParserContext {

    public WmsPullParserContext() {
        this.registerParsableModels("");
        this.registerParsableModels("http://www.opengis.net/wms");
    }

    protected void registerParsableModels(String namespaceUri) {
        this.registerParsableModel(new QName(namespaceUri, "Abstract"), TextModel.class);
        this.registerParsableModel(new QName(namespaceUri, "AccessConstraints"), TextModel.class);
        this.registerParsableModel(new QName(namespaceUri, "Address"), TextModel.class);
        this.registerParsableModel(new QName(namespaceUri, "AddressType"), TextModel.class);
        this.registerParsableModel(new QName(namespaceUri, "Attribution"), WmsLayerAttribution.class);
        this.registerParsableModel(new QName(namespaceUri, "AuthorityURL"), WmsAuthorityUrl.class);

        this.registerParsableModel(new QName(namespaceUri, "BoundingBox"), WmsBoundingBox.class);

        this.registerParsableModel(new QName(namespaceUri, "Capability"), WmsCapabilityInformation.class);
        this.registerParsableModel(new QName(namespaceUri, "City"), TextModel.class);
        this.registerParsableModel(new QName(namespaceUri, "ContactAddress"), WmsAddress.class);
        this.registerParsableModel(new QName(namespaceUri, "ContactElectronicMailAddress"), TextModel.class);
        this.registerParsableModel(new QName(namespaceUri, "ContactInformation"), WmsContactInformation.class);
        this.registerParsableModel(new QName(namespaceUri, "ContactOrganization"), TextModel.class);
        this.registerParsableModel(new QName(namespaceUri, "ContactPerson"), TextModel.class);
        this.registerParsableModel(new QName(namespaceUri, "ContactPersonPrimary"), WmsContactPersonPrimary.class);
        this.registerParsableModel(new QName(namespaceUri, "ContactPosition"), TextModel.class);
        this.registerParsableModel(new QName(namespaceUri, "ContactVoiceTelephone"), TextModel.class);
        this.registerParsableModel(new QName(namespaceUri, "Country"), TextModel.class);
        this.registerParsableModel(new QName(namespaceUri, "CRS"), TextModel.class);

        this.registerParsableModel(new QName(namespaceUri, "DataURL"), WmsLayerInfoUrl.class);
        this.registerParsableModel(new QName(namespaceUri, "DCPType"), WmsDcpType.class);
        this.registerParsableModel(new QName(namespaceUri, "Dimension"), WmsLayerDimension.class);

        this.registerParsableModel(new QName(namespaceUri, "Extent"), WmsLayerDimension.class);
        this.registerParsableModel(new QName(namespaceUri, "EX_GeographicBoundingBox"), WmsGeographicBoundingBox.class);
        this.registerParsableModel(new QName(namespaceUri, "westBoundLongitude"), DoubleModel.class);
        this.registerParsableModel(new QName(namespaceUri, "eastBoundLongitude"), DoubleModel.class);
        this.registerParsableModel(new QName(namespaceUri, "northBoundLatitude"), DoubleModel.class);
        this.registerParsableModel(new QName(namespaceUri, "southBoundLatitude"), DoubleModel.class);

        this.registerParsableModel(new QName(namespaceUri, "Exception"), WmsException.class);

        this.registerParsableModel(new QName(namespaceUri, "FeatureListURL"), WmsLayerInfoUrl.class);
        this.registerParsableModel(new QName(namespaceUri, "Fees"), TextModel.class);
        this.registerParsableModel(new QName(namespaceUri, "Format"), WmsFormat.class);

        this.registerParsableModel(new QName(namespaceUri, "Get"), WmsDcpType.WmsDcpHttpProtocol.class);
        this.registerParsableModel(new QName(namespaceUri, "GetCapabilities"), WmsRequestOperation.class);
        this.registerParsableModel(new QName(namespaceUri, "GetMap"), WmsRequestOperation.class);
        this.registerParsableModel(new QName(namespaceUri, "GetFeatureInfo"), WmsRequestOperation.class);

        this.registerParsableModel(new QName(namespaceUri, "HTTP"), WmsDcpType.WmsDcpHttp.class);

        this.registerParsableModel(new QName(namespaceUri, "Identifier"), WmsLayerIdentifier.class);

        this.registerParsableModel(new QName(namespaceUri, "Keyword"), TextModel.class);
        this.registerParsableModel(new QName(namespaceUri, "KeywordList"), WmsKeywords.class);

        this.registerParsableModel(new QName(namespaceUri, "LatLonBoundingBox"), WmsGeographicBoundingBox.class);
        this.registerParsableModel(new QName(namespaceUri, "Layer"), WmsLayerCapabilities.class);
        this.registerParsableModel(new QName(namespaceUri, "LayerInfo"), WmsLayerInfoUrl.class);
        this.registerParsableModel(new QName(namespaceUri, "LayerLimit"), IntegerModel.class);
        this.registerParsableModel(new QName(namespaceUri, "LegendURL"), WmsLogoUrl.class);
        this.registerParsableModel(new QName(namespaceUri, "LogoURL"), WmsLogoUrl.class);

        this.registerParsableModel(new QName(namespaceUri, "MaxHeight"), IntegerModel.class);
        this.registerParsableModel(new QName(namespaceUri, "MaxScaleDenominator"), DoubleModel.class);
        this.registerParsableModel(new QName(namespaceUri, "MaxWidth"), IntegerModel.class);
        this.registerParsableModel(new QName(namespaceUri, "MetadataURL"), WmsLayerInfoUrl.class);
        this.registerParsableModel(new QName(namespaceUri, "MinScaleDenominator"), DoubleModel.class);

        this.registerParsableModel(new QName(namespaceUri, "Name"), TextModel.class);

        this.registerParsableModel(new QName(namespaceUri, "OnlineResource"), WmsOnlineResource.class);

        this.registerParsableModel(new QName(namespaceUri, "Post"), WmsDcpType.WmsDcpHttpProtocol.class);
        this.registerParsableModel(new QName(namespaceUri, "PostCode"), TextModel.class);

        this.registerParsableModel(new QName(namespaceUri, "Request"), WmsRequestInformation.class);

        this.registerParsableModel(new QName(namespaceUri, "ScaleHint"), WmsScaleHint.class);
        this.registerParsableModel(new QName(namespaceUri, "Service"), WmsServiceInformation.class);
        this.registerParsableModel(new QName(namespaceUri, "SRS"), TextModel.class);
        this.registerParsableModel(new QName(namespaceUri, "StateOrProvince"), TextModel.class);
        this.registerParsableModel(new QName(namespaceUri, "Style"), WmsLayerStyle.class);
        this.registerParsableModel(new QName(namespaceUri, "StyleSheetURL"), WmsLayerInfoUrl.class);
        this.registerParsableModel(new QName(namespaceUri, "StyleURL"), WmsLayerInfoUrl.class);

        this.registerParsableModel(new QName(namespaceUri, "Title"), TextModel.class);

        this.registerParsableModel(new QName(namespaceUri, "WMS_Capabilities"), WmsCapabilities.class);
        this.registerParsableModel(new QName(namespaceUri, "WMT_MS_Capabilities"), WmsCapabilities.class);
    }
}
