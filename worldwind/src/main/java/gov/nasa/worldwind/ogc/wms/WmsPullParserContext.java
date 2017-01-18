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
        // Wms Element Registration
        this.registerParsableModel(new QName(namespaceUri, "Abstract"), new TextModel());
        this.registerParsableModel(new QName(namespaceUri, "AccessConstraints"), new TextModel());
        this.registerParsableModel(new QName(namespaceUri, "Address"), new TextModel());
        this.registerParsableModel(new QName(namespaceUri, "AddressType"), new TextModel());
        this.registerParsableModel(new QName(namespaceUri, "Attribution"), new WmsLayerAttribution());
        this.registerParsableModel(new QName(namespaceUri, "AuthorityURL"), new WmsAuthorityUrl());

        this.registerParsableModel(new QName(namespaceUri, "BoundingBox"), new WmsBoundingBox());

        this.registerParsableModel(new QName(namespaceUri, "Capability"), new WmsCapabilityInformation());
        this.registerParsableModel(new QName(namespaceUri, "City"), new TextModel());
        this.registerParsableModel(new QName(namespaceUri, "ContactAddress"), new WmsAddress());
        this.registerParsableModel(new QName(namespaceUri, "ContactElectronicMailAddress"), new TextModel());
        this.registerParsableModel(new QName(namespaceUri, "ContactInformation"), new WmsContactInformation());
        this.registerParsableModel(new QName(namespaceUri, "ContactOrganization"), new TextModel());
        this.registerParsableModel(new QName(namespaceUri, "ContactPerson"), new TextModel());
        this.registerParsableModel(new QName(namespaceUri, "ContactPersonPrimary"), new WmsContactPersonPrimary());
        this.registerParsableModel(new QName(namespaceUri, "ContactPosition"), new TextModel());
        this.registerParsableModel(new QName(namespaceUri, "ContactVoiceTelephone"), new TextModel());
        this.registerParsableModel(new QName(namespaceUri, "Country"), new TextModel());
        this.registerParsableModel(new QName(namespaceUri, "CRS"), new TextModel());

        this.registerParsableModel(new QName(namespaceUri, "DataURL"), new WmsLayerInfoUrl());
        this.registerParsableModel(new QName(namespaceUri, "DCPType"), new WmsDcpType());
        this.registerParsableModel(new QName(namespaceUri, "Dimension"), new WmsLayerDimension());

        this.registerParsableModel(new QName(namespaceUri, "Extent"), new WmsLayerDimension());
        this.registerParsableModel(new QName(namespaceUri, "EX_GeographicBoundingBox"), new WmsGeographicBoundingBox());
        this.registerParsableModel(new QName(namespaceUri, "westBoundLongitude"), new DoubleModel());
        this.registerParsableModel(new QName(namespaceUri, "eastBoundLongitude"), new DoubleModel());
        this.registerParsableModel(new QName(namespaceUri, "northBoundLatitude"), new DoubleModel());
        this.registerParsableModel(new QName(namespaceUri, "southBoundLatitude"), new DoubleModel());

        this.registerParsableModel(new QName(namespaceUri, "Exception"), new WmsException());

        this.registerParsableModel(new QName(namespaceUri, "FeatureListURL"), new WmsLayerInfoUrl());
        this.registerParsableModel(new QName(namespaceUri, "Fees"), new TextModel());
        this.registerParsableModel(new QName(namespaceUri, "Format"), new WmsFormat());

        this.registerParsableModel(new QName(namespaceUri, "Get"), new WmsDcpType.WmsDcpHttpProtocol());
        this.registerParsableModel(new QName(namespaceUri, "GetCapabilities"), new WmsRequestOperation());
        this.registerParsableModel(new QName(namespaceUri, "GetMap"), new WmsRequestOperation());
        this.registerParsableModel(new QName(namespaceUri, "GetFeatureInfo"), new WmsRequestOperation());

        this.registerParsableModel(new QName(namespaceUri, "HTTP"), new WmsDcpType.WmsDcpHttp());

        this.registerParsableModel(new QName(namespaceUri, "Identifier"), new WmsLayerIdentifier());

        this.registerParsableModel(new QName(namespaceUri, "Keyword"), new TextModel());
        this.registerParsableModel(new QName(namespaceUri, "KeywordList"), new WmsKeywords());

        this.registerParsableModel(new QName(namespaceUri, "LatLonBoundingBox"), new WmsGeographicBoundingBox());
        this.registerParsableModel(new QName(namespaceUri, "Layer"), new WmsLayerCapabilities());
        this.registerParsableModel(new QName(namespaceUri, "LayerInfo"), new WmsLayerInfoUrl());
        this.registerParsableModel(new QName(namespaceUri, "LayerLimit"), new IntegerModel());
        this.registerParsableModel(new QName(namespaceUri, "LegendURL"), new WmsLogoUrl());
        this.registerParsableModel(new QName(namespaceUri, "LogoURL"), new WmsLogoUrl());

        this.registerParsableModel(new QName(namespaceUri, "MaxHeight"), new IntegerModel());
        this.registerParsableModel(new QName(namespaceUri, "MaxScaleDenominator"), new DoubleModel());
        this.registerParsableModel(new QName(namespaceUri, "MaxWidth"), new IntegerModel());
        this.registerParsableModel(new QName(namespaceUri, "MetadataURL"), new WmsLayerInfoUrl());
        this.registerParsableModel(new QName(namespaceUri, "MinScaleDenominator"), new DoubleModel());

        this.registerParsableModel(new QName(namespaceUri, "Name"), new TextModel());

        this.registerParsableModel(new QName(namespaceUri, "OnlineResource"), new WmsOnlineResource());

        this.registerParsableModel(new QName(namespaceUri, "Post"), new WmsDcpType.WmsDcpHttpProtocol());
        this.registerParsableModel(new QName(namespaceUri, "PostCode"), new TextModel());

        this.registerParsableModel(new QName(namespaceUri, "Request"), new WmsRequestInformation());

        this.registerParsableModel(new QName(namespaceUri, "ScaleHint"), new WmsScaleHint());
        this.registerParsableModel(new QName(namespaceUri, "Service"), new WmsServiceInformation());
        this.registerParsableModel(new QName(namespaceUri, "SRS"), new TextModel());
        this.registerParsableModel(new QName(namespaceUri, "StateOrProvince"), new TextModel());
        this.registerParsableModel(new QName(namespaceUri, "Style"), new WmsLayerStyle());
        this.registerParsableModel(new QName(namespaceUri, "StyleSheetURL"), new WmsLayerInfoUrl());
        this.registerParsableModel(new QName(namespaceUri, "StyleURL"), new WmsLayerInfoUrl());

        this.registerParsableModel(new QName(namespaceUri, "Title"), new TextModel());

        this.registerParsableModel(new QName(namespaceUri, "WMS_Capabilities"), new WmsCapabilities());
        this.registerParsableModel(new QName(namespaceUri, "WMT_MS_Capabilities"), new WmsCapabilities());
    }
}
