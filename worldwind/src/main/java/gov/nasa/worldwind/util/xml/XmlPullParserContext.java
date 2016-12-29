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
import gov.nasa.worldwind.ogc.wms.WmsAuthorityUrl;
import gov.nasa.worldwind.ogc.wms.WmsBoundingBox;
import gov.nasa.worldwind.ogc.wms.WmsDcpType;
import gov.nasa.worldwind.ogc.wms.WmsLayerAttribution;
import gov.nasa.worldwind.ogc.wms.WmsLayerExtent;
import gov.nasa.worldwind.ogc.wms.WmsLayerInfoUrl;
import gov.nasa.worldwind.ogc.wms.WmsLayerStyle;
import gov.nasa.worldwind.ogc.wms.WmsLogoUrl;
import gov.nasa.worldwind.ogc.wms.WmsOnlineResource;
import gov.nasa.worldwind.ogc.wms.WmsRequestDescription;

public class XmlPullParserContext {

    /**
     * Identifies the name of the parser handling unrecognized elements. Can be used to explicitly specify the context's
     * parser-table entry for unrecognized elements.
     */
    public final static String UNRECOGNIZED_ELEMENT_PARSER = "gov.nasa.worldwind.util.xml.UnknownElementParser";

    public final static String DEFAULT_NAMESPACE = "http://www.opengis.net/wms";

    protected XmlPullParser parser;

    protected Map<QName, XmlModel> parserModels = new HashMap<>();

    protected String defaultNamespaceUri;

    public XmlPullParserContext(String defaultNamespaceUri) {

        this.defaultNamespaceUri = defaultNamespaceUri;

        this.initializeParsers();
    }

    public void setParserInput(InputStream is) throws XmlPullParserException {

        this.parser = Xml.newPullParser();
        this.parser.setInput(is, null);
    }

    protected void initializeParsers() {

        // Wms Element Registration
        this.registerParsableModel(
            new QName(this.defaultNamespaceUri, "ContactAddress"), new WmsAddress(this.defaultNamespaceUri));
        // TODO check wms schema for element name
        this.registerParsableModel(
            new QName(this.defaultNamespaceUri, "AuthorityUrl"), new WmsAuthorityUrl(this.defaultNamespaceUri));
        this.registerParsableModel(
            new QName(this.defaultNamespaceUri, "BoundingBox"), new WmsBoundingBox(this.defaultNamespaceUri));
        this.registerParsableModel(
            new QName(this.defaultNamespaceUri, "DCPType"), new WmsDcpType(this.defaultNamespaceUri));
        // TODO check wms schema for element name
        this.registerParsableModel(
            new QName(this.defaultNamespaceUri, "LayerInfo"), new WmsLayerInfoUrl(this.defaultNamespaceUri));
        this.registerParsableModel(
            new QName(this.defaultNamespaceUri, "OnlineResource"), new WmsOnlineResource(this.defaultNamespaceUri));
        this.registerParsableModel(
            new QName(this.defaultNamespaceUri, "LogoURL"), new WmsLogoUrl(this.defaultNamespaceUri));
        this.registerParsableModel(
            new QName(this.defaultNamespaceUri, "Attribution"), new WmsLayerAttribution(this.defaultNamespaceUri));
        this.registerParsableModel(new QName(this.defaultNamespaceUri, "AddressType"), new XmlModel(this.defaultNamespaceUri));
        this.registerParsableModel(new QName(this.defaultNamespaceUri, "Address"), new XmlModel(this.defaultNamespaceUri));
        this.registerParsableModel(new QName(this.defaultNamespaceUri, "City"), new XmlModel(this.defaultNamespaceUri));
        this.registerParsableModel(new QName(this.defaultNamespaceUri, "StateOrProvince"), new XmlModel(this.defaultNamespaceUri));
        this.registerParsableModel(new QName(this.defaultNamespaceUri, "PostCode"), new XmlModel(this.defaultNamespaceUri));
        this.registerParsableModel(new QName(this.defaultNamespaceUri, "Country"), new XmlModel(this.defaultNamespaceUri));

        this.registerParsableModel(new QName(this.defaultNamespaceUri, "HTTP"), new NameStringModel(this.defaultNamespaceUri));
        this.registerParsableModel(new QName(this.defaultNamespaceUri, "Get"), new NameStringModel(this.defaultNamespaceUri));
        this.registerParsableModel(new QName(this.defaultNamespaceUri, "Post"), new NameStringModel(this.defaultNamespaceUri));

        this.registerParsableModel(new QName(this.defaultNamespaceUri, "Extent"), new WmsLayerExtent(this.defaultNamespaceUri));

        this.registerParsableModel(new QName(this.defaultNamespaceUri, "Style"), new WmsLayerStyle(this.defaultNamespaceUri));
        this.registerParsableModel(new QName(this.defaultNamespaceUri, "LegendURL"), new WmsLogoUrl(this.defaultNamespaceUri));
        this.registerParsableModel(new QName(this.defaultNamespaceUri, "StyleSheetURL"), new WmsLayerInfoUrl(this.defaultNamespaceUri));
        this.registerParsableModel(new QName(this.defaultNamespaceUri, "StyleURL"), new WmsLayerInfoUrl(this.defaultNamespaceUri));

        this.registerParsableModel(new QName(this.defaultNamespaceUri, "GetCapabilities"), new WmsRequestDescription(this.defaultNamespaceUri));
        this.registerParsableModel(new QName(this.defaultNamespaceUri, "GetMap"), new WmsRequestDescription(this.defaultNamespaceUri));
        this.registerParsableModel(new QName(this.defaultNamespaceUri, "GetFeatureInfo"), new WmsRequestDescription(this.defaultNamespaceUri));

        this.registerParsableModel(new QName(this.defaultNamespaceUri, "ContactInformation"), new XmlModel(this.defaultNamespaceUri));
        this.registerParsableModel(new QName(this.defaultNamespaceUri, "ContactPersonPrimary"), new XmlModel(this.defaultNamespaceUri));
        this.registerParsableModel(new QName(this.defaultNamespaceUri, "ContactPerson"), new XmlModel(this.defaultNamespaceUri));
        this.registerParsableModel(new QName(this.defaultNamespaceUri, "ContactOrganization"), new XmlModel(this.defaultNamespaceUri));
        this.registerParsableModel(new QName(this.defaultNamespaceUri, "ContactPosition"), new XmlModel(this.defaultNamespaceUri));
        this.registerParsableModel(new QName(this.defaultNamespaceUri, "ContactVoiceTelephone"), new XmlModel(this.defaultNamespaceUri));
        this.registerParsableModel(new QName(this.defaultNamespaceUri, "ContactElectronicMailAddress"), new XmlModel(this.defaultNamespaceUri));
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
        return new XmlModel(this.defaultNamespaceUri);
    }

    public boolean isStartElement(QName event) throws XmlPullParserException, IOException {
        return (this.getParser().getEventType() == XmlPullParser.START_TAG
            && this.getParser().getName() != null
            && this.getParser().getName().equals(event.getLocalPart())
            && this.getParser().getNamespace().equals(event.getNamespaceURI()));
    }

}
