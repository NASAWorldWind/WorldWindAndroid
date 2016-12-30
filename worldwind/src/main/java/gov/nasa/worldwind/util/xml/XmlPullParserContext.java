/*
 * Copyright (c) 2016 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.util.xml;

import android.util.Log;
import android.util.Xml;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;

import javax.xml.namespace.QName;

import gov.nasa.worldwind.ogc.wms.WmsAddress;
import gov.nasa.worldwind.ogc.wms.WmsAuthorityUrl2;
import gov.nasa.worldwind.ogc.wms.WmsBoundingBox;
import gov.nasa.worldwind.ogc.wms.WmsCapabilities;
import gov.nasa.worldwind.ogc.wms.WmsCapabilityInformation;
import gov.nasa.worldwind.ogc.wms.WmsContactInformation;
import gov.nasa.worldwind.ogc.wms.WmsDcpType2;
import gov.nasa.worldwind.ogc.wms.WmsGeographicBoundingBox;
import gov.nasa.worldwind.ogc.wms.WmsKeywords;
import gov.nasa.worldwind.ogc.wms.WmsLayerAttribution;
import gov.nasa.worldwind.ogc.wms.WmsLayerCapabilities;
import gov.nasa.worldwind.ogc.wms.WmsLayerDimension;
import gov.nasa.worldwind.ogc.wms.WmsLayerExtent;
import gov.nasa.worldwind.ogc.wms.WmsLayerIdentifier;
import gov.nasa.worldwind.ogc.wms.WmsLayerInfoUrl2;
import gov.nasa.worldwind.ogc.wms.WmsLayerStyle;
import gov.nasa.worldwind.ogc.wms.WmsLogoUrl2;
import gov.nasa.worldwind.ogc.wms.WmsOnlineResource;
import gov.nasa.worldwind.ogc.wms.WmsRequestDescription;
import gov.nasa.worldwind.ogc.wms.WmsServiceInformation;

public class XmlPullParserContext {

    /**
     * Identifies the name of the parser handling unrecognized elements. Can be used to explicitly specify the context's
     * parser-table entry for unrecognized elements.
     */
    public final static String UNRECOGNIZED_ELEMENT_PARSER = "gov.nasa.worldwind.util.xml.UnknownElementParser";

    public final static String DEFAULT_NAMESPACE = "http://www.opengis.net/wms";

    protected XmlPullParser parser;

    protected Map<QName, XmlModel> parserModels = new HashMap<>();

    protected String namespaceUri;

    public XmlPullParserContext(String namespaceUri) {

        this.namespaceUri = namespaceUri;

        this.initializeParsers();
    }

    public void setParserInput(InputStream is) throws XmlPullParserException {

        this.parser = Xml.newPullParser();
        this.parser.setInput(is, null);
    }

    protected void initializeParsers() {

        // Wms Element Registration
        this.registerParsableModel(new QName(this.namespaceUri, "ContactAddress"), new WmsAddress(this.namespaceUri));
        // TODO check wms schema for element name
        this.registerParsableModel(new QName(this.namespaceUri, "AuthorityUrl"), new WmsAuthorityUrl2(this.namespaceUri));
        this.registerParsableModel(new QName(this.namespaceUri, "BoundingBox"), new WmsBoundingBox(this.namespaceUri));
        this.registerParsableModel(new QName(this.namespaceUri, "DCPType"), new WmsDcpType2(this.namespaceUri));
        // TODO check wms schema for element name
        this.registerParsableModel(new QName(this.namespaceUri, "LayerInfo"), new WmsLayerInfoUrl2(this.namespaceUri));
        this.registerParsableModel(new QName(this.namespaceUri, "OnlineResource"), new WmsOnlineResource(this.namespaceUri));
        this.registerParsableModel(new QName(this.namespaceUri, "LogoURL"), new WmsLogoUrl2(this.namespaceUri));
        this.registerParsableModel(new QName(this.namespaceUri, "Attribution"), new WmsLayerAttribution(this.namespaceUri));
        this.registerParsableModel(new QName(this.namespaceUri, "AddressType"), new XmlModel(this.namespaceUri));
        this.registerParsableModel(new QName(this.namespaceUri, "Address"), new XmlModel(this.namespaceUri));
        this.registerParsableModel(new QName(this.namespaceUri, "City"), new XmlModel(this.namespaceUri));
        this.registerParsableModel(new QName(this.namespaceUri, "StateOrProvince"), new XmlModel(this.namespaceUri));
        this.registerParsableModel(new QName(this.namespaceUri, "PostCode"), new XmlModel(this.namespaceUri));
        this.registerParsableModel(new QName(this.namespaceUri, "Country"), new XmlModel(this.namespaceUri));

        this.registerParsableModel(new QName(this.namespaceUri, "HTTP"), new NameStringModel(this.namespaceUri));
        this.registerParsableModel(new QName(this.namespaceUri, "Get"), new NameStringModel(this.namespaceUri));
        this.registerParsableModel(new QName(this.namespaceUri, "Post"), new NameStringModel(this.namespaceUri));

        this.registerParsableModel(new QName(this.namespaceUri, "Extent"), new WmsLayerExtent(this.namespaceUri));

        this.registerParsableModel(new QName(this.namespaceUri, "Style"), new WmsLayerStyle(this.namespaceUri));
        this.registerParsableModel(new QName(this.namespaceUri, "LegendURL"), new WmsLogoUrl2(this.namespaceUri));
        this.registerParsableModel(new QName(this.namespaceUri, "StyleSheetURL"), new WmsLayerInfoUrl2(this.namespaceUri));
        this.registerParsableModel(new QName(this.namespaceUri, "StyleURL"), new WmsLayerInfoUrl2(this.namespaceUri));

        this.registerParsableModel(new QName(this.namespaceUri, "GetCapabilities"), new WmsRequestDescription(this.namespaceUri));
        this.registerParsableModel(new QName(this.namespaceUri, "GetMap"), new WmsRequestDescription(this.namespaceUri));
        this.registerParsableModel(new QName(this.namespaceUri, "GetFeatureInfo"), new WmsRequestDescription(this.namespaceUri));

        this.registerParsableModel(new QName(this.namespaceUri, "ContactInformation"), new WmsContactInformation(this.namespaceUri));
        this.registerParsableModel(new QName(this.namespaceUri, "ContactPersonPrimary"), new XmlModel(this.namespaceUri));
        this.registerParsableModel(new QName(this.namespaceUri, "ContactPerson"), new XmlModel(this.namespaceUri));
        this.registerParsableModel(new QName(this.namespaceUri, "ContactOrganization"), new XmlModel(this.namespaceUri));
        this.registerParsableModel(new QName(this.namespaceUri, "ContactPosition"), new XmlModel(this.namespaceUri));
        this.registerParsableModel(new QName(this.namespaceUri, "ContactVoiceTelephone"), new XmlModel(this.namespaceUri));
        this.registerParsableModel(new QName(this.namespaceUri, "ContactElectronicMailAddress"), new XmlModel(this.namespaceUri));

        this.registerParsableModel(new QName(this.namespaceUri, "KeywordList"), new WmsKeywords(this.namespaceUri));
        this.registerParsableModel(new QName(this.namespaceUri, "MaxHeight"), new IntegerModel(this.namespaceUri));
        this.registerParsableModel(new QName(this.namespaceUri, "MaxWidth"), new IntegerModel(this.namespaceUri));
        this.registerParsableModel(new QName(this.namespaceUri, "LayerLimit"), new IntegerModel(this.namespaceUri));

        this.registerParsableModel(new QName(this.namespaceUri, "Service"), new WmsServiceInformation(this.namespaceUri));

        this.registerParsableModel(new QName(this.namespaceUri, "Layer"), new WmsLayerCapabilities(this.namespaceUri));
        this.registerParsableModel(new QName(this.namespaceUri, "MinScaleDenominator"), new DoubleModel(this.namespaceUri));
        this.registerParsableModel(new QName(this.namespaceUri, "MaxScaleDenominator"), new DoubleModel(this.namespaceUri));
        this.registerParsableModel(new QName(this.namespaceUri, "EX_GeographicBoundingBox"), new WmsGeographicBoundingBox(this.namespaceUri));
        this.registerParsableModel(new QName(this.namespaceUri, "westBoundLongitude"), new DoubleModel(this.namespaceUri));
        this.registerParsableModel(new QName(this.namespaceUri, "eastBoundLongitude"), new DoubleModel(this.namespaceUri));
        this.registerParsableModel(new QName(this.namespaceUri, "northBoundLatitude"), new DoubleModel(this.namespaceUri));
        this.registerParsableModel(new QName(this.namespaceUri, "southBoundLatitude"), new DoubleModel(this.namespaceUri));
        this.registerParsableModel(new QName(this.namespaceUri, "MetadataURL"), new WmsLayerInfoUrl2(this.namespaceUri));
        this.registerParsableModel(new QName(this.namespaceUri, "FeatureListURL"), new WmsLayerInfoUrl2(this.namespaceUri));
        this.registerParsableModel(new QName(this.namespaceUri, "DataURL"), new WmsLayerInfoUrl2(this.namespaceUri));
        this.registerParsableModel(new QName(this.namespaceUri, "Identifier"), new WmsLayerIdentifier(this.namespaceUri));
        this.registerParsableModel(new QName(this.namespaceUri, "Dimension"), new WmsLayerDimension(this.namespaceUri));

        this.registerParsableModel(new QName(this.namespaceUri, "Capability"), new WmsCapabilityInformation(this.namespaceUri));

        this.registerParsableModel(new QName(this.namespaceUri, "WMT_MS_Capabilities"), new WmsCapabilities(this.namespaceUri));
        this.registerParsableModel(new QName(this.namespaceUri, "WMS_Capabilities"), new WmsCapabilities(this.namespaceUri));
    }

    //There was limited use, and I'm trying to abstract the XmlPullParser from consumers
    //public XmlPullParser getEventReader();

    // Not seeing use in the Wms code
    public XmlPullParser getParser() {
        return this.parser;
    }

    /**
     * Returns a new parser for a specified element name.
     *
     * @param eventName indicates the element name for which a parser is created.
     *
     * @return the new parser, or null if no parser has been registered for the specified element name.
     */
    public XmlModel createParsableModel(QName eventName) {

        XmlModel model = this.parserModels.get(eventName);

        // If no model is specified use the default class and the provided QName namespace
        if (model == null) {
            return new XmlModel(eventName.getNamespaceURI());
        }

        try {
            // create a duplicate instance using reflective utilities
            Constructor<? extends XmlModel> ctor = model.getClass().getDeclaredConstructor(String.class);
            //ctor.setAccessible(true);
            return ctor.newInstance(eventName.getNamespaceURI());
        } catch (Exception e) {
            // TODO log error
            Log.e("gov.nasa.worldwind", e.toString());
        }

        return null;
    }

    /**
     * Registers a parser for a specified element name. A parser of the same type and namespace is returned when is
     * called for the same element name.
     */
    public void registerParsableModel(QName elementName, XmlModel parsableModel) {

        this.parserModels.put(elementName, parsableModel);

    }

    public XmlModel getUnrecognizedElementModel() {
        return new XmlModel(this.namespaceUri);
    }

    public boolean isStartElement(QName event) throws XmlPullParserException, IOException {
        return (this.getParser().getEventType() == XmlPullParser.START_TAG
            && this.getParser().getName() != null
            && this.getParser().getName().equals(event.getLocalPart())
            && this.getParser().getNamespace().equals(event.getNamespaceURI()));
    }

    public String getNamespaceUri() {
        return namespaceUri;
    }

    public void setNamespaceUri(String namespaceUri) {
        this.namespaceUri = namespaceUri;
        this.parserModels.clear();
        this.initializeParsers();
    }
}
