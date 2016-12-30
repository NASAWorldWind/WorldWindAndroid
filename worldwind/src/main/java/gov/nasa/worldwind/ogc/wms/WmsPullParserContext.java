/*
 * Copyright (c) 2016 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.ogc.wms;

import javax.xml.namespace.QName;

import gov.nasa.worldwind.util.xml.DoubleModel;
import gov.nasa.worldwind.util.xml.IntegerModel;
import gov.nasa.worldwind.util.xml.NameStringModel;
import gov.nasa.worldwind.util.xml.XmlModel;
import gov.nasa.worldwind.util.xml.XmlPullParserContext;

public class WmsPullParserContext extends XmlPullParserContext {

    public WmsPullParserContext(String namespaceUri) {
        super(namespaceUri);
    }

    @Override
    protected void initializeParsers() {

        super.initializeParsers();

        // Wms Element Registration
        this.registerParsableModel(new QName(this.namespaceUri, "Address"), new XmlModel(this.namespaceUri));
        this.registerParsableModel(new QName(this.namespaceUri, "AddressType"), new XmlModel(this.namespaceUri));
        this.registerParsableModel(new QName(this.namespaceUri, "Attribution"), new WmsLayerAttribution(this.namespaceUri));
        this.registerParsableModel(new QName(this.namespaceUri, "AuthorityUrl"), new WmsAuthorityUrl(this.namespaceUri));

        this.registerParsableModel(new QName(this.namespaceUri, "BoundingBox"), new WmsBoundingBox(this.namespaceUri));

        this.registerParsableModel(new QName(this.namespaceUri, "Capability"), new WmsCapabilityInformation(this.namespaceUri));
        this.registerParsableModel(new QName(this.namespaceUri, "City"), new XmlModel(this.namespaceUri));
        this.registerParsableModel(new QName(this.namespaceUri, "ContactAddress"), new WmsAddress(this.namespaceUri));
        this.registerParsableModel(new QName(this.namespaceUri, "ContactElectronicMailAddress"), new XmlModel(this.namespaceUri));
        this.registerParsableModel(new QName(this.namespaceUri, "ContactInformation"), new WmsContactInformation(this.namespaceUri));
        this.registerParsableModel(new QName(this.namespaceUri, "ContactOrganization"), new XmlModel(this.namespaceUri));
        this.registerParsableModel(new QName(this.namespaceUri, "ContactPerson"), new XmlModel(this.namespaceUri));
        this.registerParsableModel(new QName(this.namespaceUri, "ContactPersonPrimary"), new XmlModel(this.namespaceUri));
        this.registerParsableModel(new QName(this.namespaceUri, "ContactPosition"), new XmlModel(this.namespaceUri));
        this.registerParsableModel(new QName(this.namespaceUri, "ContactVoiceTelephone"), new XmlModel(this.namespaceUri));
        this.registerParsableModel(new QName(this.namespaceUri, "Country"), new XmlModel(this.namespaceUri));

        this.registerParsableModel(new QName(this.namespaceUri, "DataURL"), new WmsLayerInfoUrl(this.namespaceUri));
        this.registerParsableModel(new QName(this.namespaceUri, "DCPType"), new WmsDcpType(this.namespaceUri));
        this.registerParsableModel(new QName(this.namespaceUri, "Dimension"), new WmsLayerDimension(this.namespaceUri));

        this.registerParsableModel(new QName(this.namespaceUri, "Extent"), new WmsLayerExtent(this.namespaceUri));
        this.registerParsableModel(new QName(this.namespaceUri, "EX_GeographicBoundingBox"), new WmsGeographicBoundingBox(this.namespaceUri));
        this.registerParsableModel(new QName(this.namespaceUri, "westBoundLongitude"), new DoubleModel(this.namespaceUri));
        this.registerParsableModel(new QName(this.namespaceUri, "eastBoundLongitude"), new DoubleModel(this.namespaceUri));
        this.registerParsableModel(new QName(this.namespaceUri, "northBoundLatitude"), new DoubleModel(this.namespaceUri));
        this.registerParsableModel(new QName(this.namespaceUri, "southBoundLatitude"), new DoubleModel(this.namespaceUri));

        this.registerParsableModel(new QName(this.namespaceUri, "FeatureListURL"), new WmsLayerInfoUrl(this.namespaceUri));

        this.registerParsableModel(new QName(this.namespaceUri, "Get"), new NameStringModel(this.namespaceUri));
        this.registerParsableModel(new QName(this.namespaceUri, "GetCapabilities"), new WmsRequestDescription(this.namespaceUri));
        this.registerParsableModel(new QName(this.namespaceUri, "GetMap"), new WmsRequestDescription(this.namespaceUri));
        this.registerParsableModel(new QName(this.namespaceUri, "GetFeatureInfo"), new WmsRequestDescription(this.namespaceUri));

        this.registerParsableModel(new QName(this.namespaceUri, "HTTP"), new NameStringModel(this.namespaceUri));

        this.registerParsableModel(new QName(this.namespaceUri, "Identifier"), new WmsLayerIdentifier(this.namespaceUri));

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

        this.registerParsableModel(new QName(this.namespaceUri, "OnlineResource"), new WmsOnlineResource(this.namespaceUri));

        this.registerParsableModel(new QName(this.namespaceUri, "Post"), new NameStringModel(this.namespaceUri));
        this.registerParsableModel(new QName(this.namespaceUri, "PostCode"), new XmlModel(this.namespaceUri));

        this.registerParsableModel(new QName(this.namespaceUri, "Service"), new WmsServiceInformation(this.namespaceUri));
        this.registerParsableModel(new QName(this.namespaceUri, "StateOrProvince"), new XmlModel(this.namespaceUri));
        this.registerParsableModel(new QName(this.namespaceUri, "Style"), new WmsLayerStyle(this.namespaceUri));
        this.registerParsableModel(new QName(this.namespaceUri, "StyleSheetURL"), new WmsLayerInfoUrl(this.namespaceUri));
        this.registerParsableModel(new QName(this.namespaceUri, "StyleURL"), new WmsLayerInfoUrl(this.namespaceUri));

        this.registerParsableModel(new QName(this.namespaceUri, "WMS_Capabilities"), new WmsCapabilities(this.namespaceUri));
        this.registerParsableModel(new QName(this.namespaceUri, "WMT_MS_Capabilities"), new WmsCapabilities(this.namespaceUri));
    }
}
