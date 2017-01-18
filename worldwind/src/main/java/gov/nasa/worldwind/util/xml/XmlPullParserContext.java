/*
 * Copyright (c) 2016 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.util.xml;

import android.util.Xml;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import javax.xml.namespace.QName;

import gov.nasa.worldwind.util.Logger;

public class XmlPullParserContext {

    protected XmlPullParser parser;

    protected Map<QName, Class<? extends XmlModel>> parsableModels = new HashMap<>();

    public XmlPullParserContext() {
    }

    public void setParserInput(InputStream is) throws XmlPullParserException {
        this.parser = Xml.newPullParser();
        this.parser.setInput(is, null);
    }

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
        Class<? extends XmlModel> clazz = this.parsableModels.get(eventName);

        if (clazz == null) {
            clazz = this.getUnrecognizedModel(); // use the unrecognized model
        }

        try {
            return clazz.newInstance(); // create a new instance using the default constructor
        } catch (Exception e) {
            Logger.logMessage(Logger.ERROR, "XmlPullParserContext", "createParsableModel",
                "Exception invoking default constructor for " + clazz.getName(), e);
        }

        return null;
    }

    /**
     * Registers a parser for a specified element name. A parser of the same type and namespace is returned when is
     * called for the same element name.
     */
    public void registerParsableModel(QName elementName, Class<? extends XmlModel> parsableModel) {
        this.parsableModels.put(elementName, parsableModel);
    }

    protected Class<? extends XmlModel> getUnrecognizedModel() {
        return DefaultXmlModel.class;
    }

    public boolean isStartElement(QName event) throws XmlPullParserException, IOException {
        return (this.getParser().getEventType() == XmlPullParser.START_TAG
            && this.getParser().getName() != null
            && this.getParser().getName().equals(event.getLocalPart())
            && this.getParser().getNamespace().equals(event.getNamespaceURI()));
    }
}
