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

    public WmsPullParserContext(String namespaceUri) {
        super(namespaceUri);
    }

    @Override
    protected void initializeParsers() {
        super.initializeParsers();

        // Wms Element Registration
        this.registerParsableModel(new QName(this.namespaceUri, "Abstract"), new TextModel(this.namespaceUri));
        this.registerParsableModel(new QName(this.namespaceUri, "AccessConstraints"), new TextModel(this.namespaceUri));
        this.registerParsableModel(new QName(this.namespaceUri, "Address"), new TextModel(this.namespaceUri));
        this.registerParsableModel(new QName(this.namespaceUri, "AddressType"), new TextModel(this.namespaceUri));
        this.registerParsableModel(new QName(this.namespaceUri, "Attribution"), new WmsLayerAttribution(this.namespaceUri));
        this.registerParsableModel(new QName(this.namespaceUri, "AuthorityURL"), new WmsAuthorityUrl(this.namespaceUri));

        this.registerParsableModel(new QName(this.namespaceUri, "BoundingBox"), new WmsBoundingBox(this.namespaceUri));

        this.registerParsableModel(new QName(this.namespaceUri, "Capability"), new WmsCapabilityInformation(this.namespaceUri));
        this.registerParsableModel(new QName(this.namespaceUri, "City"), new TextModel(this.namespaceUri));
        this.registerParsableModel(new QName(this.namespaceUri, "ContactAddress"), new WmsAddress(this.namespaceUri));
        this.registerParsableModel(new QName(this.namespaceUri, "ContactElectronicMailAddress"), new TextModel(this.namespaceUri));
        this.registerParsableModel(new QName(this.namespaceUri, "ContactInformation"), new WmsContactInformation(this.namespaceUri));
        this.registerParsableModel(new QName(this.namespaceUri, "ContactOrganization"), new TextModel(this.namespaceUri));
        this.registerParsableModel(new QName(this.namespaceUri, "ContactPerson"), new TextModel(this.namespaceUri));
        this.registerParsableModel(new QName(this.namespaceUri, "ContactPersonPrimary"), new WmsContactPersonPrimary(this.namespaceUri));
        this.registerParsableModel(new QName(this.namespaceUri, "ContactPosition"), new TextModel(this.namespaceUri));
        this.registerParsableModel(new QName(this.namespaceUri, "ContactVoiceTelephone"), new TextModel(this.namespaceUri));
        this.registerParsableModel(new QName(this.namespaceUri, "Country"), new TextModel(this.namespaceUri));
        this.registerParsableModel(new QName(this.namespaceUri, "CRS"), new TextModel(this.namespaceUri));

        this.registerParsableModel(new QName(this.namespaceUri, "DataURL"), new WmsLayerInfoUrl(this.namespaceUri));
        this.registerParsableModel(new QName(this.namespaceUri, "DCPType"), new WmsDcpType(this.namespaceUri));
        this.registerParsableModel(new QName(this.namespaceUri, "Dimension"), new WmsLayerDimension(this.namespaceUri));

        this.registerParsableModel(new QName(this.namespaceUri, "Extent"), new WmsLayerDimension(this.namespaceUri));
        this.registerParsableModel(new QName(this.namespaceUri, "EX_GeographicBoundingBox"), new WmsGeographicBoundingBox(this.namespaceUri));
        this.registerParsableModel(new QName(this.namespaceUri, "westBoundLongitude"), new DoubleModel(this.namespaceUri));
        this.registerParsableModel(new QName(this.namespaceUri, "eastBoundLongitude"), new DoubleModel(this.namespaceUri));
        this.registerParsableModel(new QName(this.namespaceUri, "northBoundLatitude"), new DoubleModel(this.namespaceUri));
        this.registerParsableModel(new QName(this.namespaceUri, "southBoundLatitude"), new DoubleModel(this.namespaceUri));

        this.registerParsableModel(new QName(this.namespaceUri, "Exception"), new WmsException(this.namespaceUri));

        this.registerParsableModel(new QName(this.namespaceUri, "FeatureListURL"), new WmsLayerInfoUrl(this.namespaceUri));
        this.registerParsableModel(new QName(this.namespaceUri, "Fees"), new TextModel(this.namespaceUri));
        this.registerParsableModel(new QName(this.namespaceUri, "Format"), new WmsFormat(this.namespaceUri));

        this.registerParsableModel(new QName(this.namespaceUri, "Get"), new WmsDcpType.WmsDcpHttpProtocol(this.namespaceUri));
        this.registerParsableModel(new QName(this.namespaceUri, "GetCapabilities"), new WmsRequestOperation(this.namespaceUri));
        this.registerParsableModel(new QName(this.namespaceUri, "GetMap"), new WmsRequestOperation(this.namespaceUri));
        this.registerParsableModel(new QName(this.namespaceUri, "GetFeatureInfo"), new WmsRequestOperation(this.namespaceUri));

        this.registerParsableModel(new QName(this.namespaceUri, "HTTP"), new WmsDcpType.WmsDcpHttp(this.namespaceUri));

        this.registerParsableModel(new QName(this.namespaceUri, "Identifier"), new WmsLayerIdentifier(this.namespaceUri));

        this.registerParsableModel(new QName(this.namespaceUri, "Keyword"), new TextModel(this.namespaceUri));
        this.registerParsableModel(new QName(this.namespaceUri, "KeywordList"), new WmsKeywords(this.namespaceUri));

        this.registerParsableModel(new QName(this.namespaceUri, "LatLonBoundingBox"), new WmsGeographicBoundingBox(this.namespaceUri));
        this.registerParsableModel(new QName(this.namespaceUri, "Layer"), new WmsLayerCapabilities(this.namespaceUri));
        this.registerParsableModel(new QName(this.namespaceUri, "LayerInfo"), new WmsLayerInfoUrl(this.namespaceUri));
        this.registerParsableModel(new QName(this.namespaceUri, "LayerLimit"), new IntegerModel(this.namespaceUri));
        this.registerParsableModel(new QName(this.namespaceUri, "LegendURL"), new WmsLogoUrl(this.namespaceUri));
        this.registerParsableModel(new QName(this.namespaceUri, "LogoURL"), new WmsLogoUrl(this.namespaceUri));

        this.registerParsableModel(new QName(this.namespaceUri, "MaxHeight"), new IntegerModel(this.namespaceUri));
        this.registerParsableModel(new QName(this.namespaceUri, "MaxScaleDenominator"), new DoubleModel(this.namespaceUri));
        this.registerParsableModel(new QName(this.namespaceUri, "MaxWidth"), new IntegerModel(this.namespaceUri));
        this.registerParsableModel(new QName(this.namespaceUri, "MetadataURL"), new WmsLayerInfoUrl(this.namespaceUri));
        this.registerParsableModel(new QName(this.namespaceUri, "MinScaleDenominator"), new DoubleModel(this.namespaceUri));

        this.registerParsableModel(new QName(this.namespaceUri, "Name"), new TextModel(this.namespaceUri));

        this.registerParsableModel(new QName(this.namespaceUri, "OnlineResource"), new WmsOnlineResource(this.namespaceUri));

        this.registerParsableModel(new QName(this.namespaceUri, "Post"), new WmsDcpType.WmsDcpHttpProtocol(this.namespaceUri));
        this.registerParsableModel(new QName(this.namespaceUri, "PostCode"), new TextModel(this.namespaceUri));

        this.registerParsableModel(new QName(this.namespaceUri, "Request"), new WmsRequestInformation(this.namespaceUri));

        this.registerParsableModel(new QName(this.namespaceUri, "ScaleHint"), new WmsScaleHint(this.namespaceUri));
        this.registerParsableModel(new QName(this.namespaceUri, "Service"), new WmsServiceInformation(this.namespaceUri));
        this.registerParsableModel(new QName(this.namespaceUri, "SRS"), new TextModel(this.namespaceUri));
        this.registerParsableModel(new QName(this.namespaceUri, "StateOrProvince"), new TextModel(this.namespaceUri));
        this.registerParsableModel(new QName(this.namespaceUri, "Style"), new WmsLayerStyle(this.namespaceUri));
        this.registerParsableModel(new QName(this.namespaceUri, "StyleSheetURL"), new WmsLayerInfoUrl(this.namespaceUri));
        this.registerParsableModel(new QName(this.namespaceUri, "StyleURL"), new WmsLayerInfoUrl(this.namespaceUri));

        this.registerParsableModel(new QName(this.namespaceUri, "Title"), new TextModel(this.namespaceUri));

        this.registerParsableModel(new QName(this.namespaceUri, "WMS_Capabilities"), new WmsCapabilities(this.namespaceUri));
        this.registerParsableModel(new QName(this.namespaceUri, "WMT_MS_Capabilities"), new WmsCapabilities(this.namespaceUri));
    }
}
