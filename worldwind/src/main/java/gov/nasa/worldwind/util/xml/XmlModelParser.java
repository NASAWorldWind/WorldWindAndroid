/*
 * Copyright (c) 2016 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.util.xml;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.xml.namespace.QName;

import gov.nasa.worldwind.util.Logger;

public class XmlModelParser {

    protected XmlPullParser xpp;

    protected XmlModel parsedModel;

    protected Map<QName, Class<? extends XmlModel>> parsableModels = new HashMap<>();

    public XmlModelParser() {
    }

    public XmlPullParser getPullParser() {
        return this.xpp;
    }

    public void setPullParser(XmlPullParser parser) {
        this.xpp = parser;
    }

    public void parse() throws XmlPullParserException, IOException {
        this.parsedModel = null;

        if (this.xpp == null) {
            return; // nothing to parse
        }

        while (this.xpp.getEventType() != XmlPullParser.START_TAG) {
            this.xpp.next(); // skip to the start of the first element
        }

        this.parsedModel = this.parseElement(null /*parent*/);
    }

    public XmlModel getParsedModel() {
        return this.parsedModel;
    }

    /**
     * Registers a xpp for a specified element name. A xpp of the same type and namespace is returned when is called for
     * the same element name.
     */
    public void registerParsableModel(String namespace, String name, Class<? extends XmlModel> parsableModel) {
        this.parsableModels.put(new QName(namespace, name), parsableModel);
    }

    /**
     * Returns a new xpp for a specified element name.
     *
     * @return the new xpp, or null if no xpp has been registered for the specified element name.
     */
    public XmlModel createParsableModel(String namespace, String name) {
        Class<? extends XmlModel> clazz = this.parsableModels.get(new QName(namespace, name));

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

    protected XmlModel parseElement(XmlModel parent) throws XmlPullParserException, IOException {
        // Create an instance of an XML model object associated with the element's namespace and tag name.
        XmlModel model = this.createParsableModel(this.xpp.getNamespace(), this.xpp.getName());
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
                String childName = this.xpp.getName(); // store the child name before recursively parsing
                XmlModel childValue = this.parseElement(model); // recursively parse the child element
                model.parseField(childName, childValue);
            } else if (this.xpp.getEventType() == XmlPullParser.TEXT) {
                String text = this.xpp.getText();
                model.parseText(text);
            }
        }

        return model;
    }
}
