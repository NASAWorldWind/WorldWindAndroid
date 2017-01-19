/*
 * Copyright (c) 2016 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.util.xml;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.xml.namespace.QName;

import gov.nasa.worldwind.util.Logger;

public class XmlModelParser {

    protected XmlPullParser xpp;

    protected Map<QName, Class<? extends XmlModel>> xmlModelRegistry = new HashMap<>();

    protected Set<QName> txtModelRegistry = new HashSet<>();

    public XmlModelParser() {
    }

    public XmlPullParser getPullParser() {
        return this.xpp;
    }

    public void setPullParser(XmlPullParser parser) {
        this.xpp = parser;
    }

    public Object parse() throws XmlPullParserException, IOException {
        if (this.xpp == null) {
            return null; // nothing to parse
        }

        while (this.xpp.getEventType() != XmlPullParser.START_TAG) {
            this.xpp.next(); // skip to the start of the first element
        }

        QName name = new QName(this.xpp.getNamespace(), this.xpp.getName());
        return this.parseElement(name, null /*parent*/);
    }

    /**
     * Registers a xpp for a specified element name. A xpp of the same type and namespace is returned when is called for
     * the same element name.
     */
    public void registerXmlModel(String namespace, String name, Class<? extends XmlModel> parsableModel) {
        this.xmlModelRegistry.put(new QName(namespace, name), parsableModel);
    }

    public void registerTxtModel(String namespace, String name) {
        this.txtModelRegistry.add(new QName(namespace, name));
    }

    /**
     * Returns a new xpp for a specified element name.
     *
     * @return the new xpp, or null if no xpp has been registered for the specified element name.
     */
    protected XmlModel createXmlModel(QName name) {
        Class<? extends XmlModel> clazz = this.xmlModelRegistry.get(name);

        if (clazz == null) {
            clazz = this.getUnrecognizedModel(); // use the unrecognized model
        }

        try {
            return clazz.newInstance(); // create a new instance using the default constructor
        } catch (Exception e) {
            Logger.logMessage(Logger.ERROR, "XmlModelParser", "createParsableModel",
                "Exception invoking default constructor for " + clazz.getName(), e);
        }

        return null;
    }

    protected Class<? extends XmlModel> getUnrecognizedModel() {
        return DefaultXmlModel.class;
    }

    protected Object parseElement(QName name, XmlModel parent) throws XmlPullParserException, IOException {
        if (this.txtModelRegistry.contains(name)) {
            return this.parseText(name);
        } else {
            return this.parseXmlModel(name, parent);
        }
    }

    protected XmlModel parseXmlModel(QName name, XmlModel parent) throws XmlPullParserException, IOException {
        // Create an instance of an XML model object associated with the element's namespace and tag name.
        XmlModel model = this.createXmlModel(name);
        model.setParent(parent);

        // Parse the element's attributes.
        for (int idx = 0, len = this.xpp.getAttributeCount(); idx < len; idx++) {
            String attrName = this.xpp.getAttributeName(idx);
            String attrValue = this.xpp.getAttributeValue(idx);
            model.parseField(attrName, attrValue);
        }

        // Parse the element's content until we reach either the end of the document or the end of the element.
        while (this.xpp.next() != XmlPullParser.END_DOCUMENT
            && this.xpp.getEventType() != XmlPullParser.END_TAG) {

            if (this.xpp.getEventType() == XmlPullParser.START_TAG) {
                QName childName = new QName(this.xpp.getNamespace(), this.xpp.getName()); // store the child name before recursively parsing
                Object childValue = this.parseElement(name, model /*parent*/); // recursively parse the child element
                model.parseField(childName.getLocalPart(), childValue);
            } else if (this.xpp.getEventType() == XmlPullParser.TEXT) {
                String text = this.xpp.getText();
                model.parseText(text);
            }
        }

        return model;
    }

    protected String parseText(QName name) throws XmlPullParserException, IOException {
        StringBuilder characters = new StringBuilder();

        // Parse the element's content until we reach either the end of the document or the end of the element.
        while (this.xpp.next() != XmlPullParser.END_DOCUMENT) {

            if (this.xpp.getEventType() == XmlPullParser.TEXT) {
                String text = this.xpp.getText();
                if (text != null) {
                    text = text.replaceAll("\n", "").trim();
                    characters.append(text);
                }
            } else if (this.xpp.getEventType() == XmlPullParser.END_TAG && this.xpp.getName().equals(name.getLocalPart())) {
                break; // we've reached the end of the text element
            }
        }

        return characters.toString();
    }
}
