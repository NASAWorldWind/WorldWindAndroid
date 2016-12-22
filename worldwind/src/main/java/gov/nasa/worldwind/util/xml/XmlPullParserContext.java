/*
 * Copyright (c) 2016 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.util.xml;

import android.util.Xml;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import javax.xml.namespace.QName;

public class XmlPullParserContext {

    /**
     * Identifies the name of the parser handling unrecognized elements. Can be used to explicitly specify the context's
     * parser-table entry for unrecognized elements.
     */
    public final static String UNRECOGNIZED_ELEMENT_PARSER = "gov.nasa.worldwind.util.xml.UnknownElementParser";

    protected XmlPullParser parser;

    protected Map<QName, XmlElementModel> parserModels = new HashMap<>();

    protected String defaultNamespaceUri = "";

    public XmlPullParserContext() {

    }

    public void setParserInput(InputStream is) throws XmlPullParserException {

        this.parser = Xml.newPullParser();
        this.parser.setInput(is, null);

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
    public XmlElementModel getParsableModel(QName eventName) {

        return this.parserModels.get(eventName);

    }

    /**
     * Registers a parser for a specified element name. A parser of the same type and namespace is returned when is
     * called for the same element name.
     */
    public void registerParsableModel(QName elementName, XmlElementModel parsableModel) {

        this.parserModels.put(elementName, parsableModel);

    }

    public XmlElementModel getUnrecognizedElementModel() {
        return null;
    }

}
