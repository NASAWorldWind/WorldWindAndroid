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

/**
 * Created by zach on 12/20/16.
 */

public class XmlElementModel {

    protected static final String CHARACTERS_CONTENT = "CharactersContent";

    protected String namespaceUri = "";

    protected Map<String, Object> fields;

    protected XmlElementModel parent;

    public XmlElementModel() {
        this.namespaceUri = null;
    }

    public XmlElementModel(String namespaceUri) {
        this.namespaceUri = namespaceUri;
    }

    public String getNamespaceUri() {
        return this.namespaceUri;
    }

    public void setNamespaceUri(String namespaceUri) {
        this.namespaceUri = namespaceUri;
    }

    public Object read(XmlPullParserContext ctx) throws XmlPullParserException, IOException {

        if (ctx == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "XmlElementModel", "parse", "missingContext"));
        }

        XmlPullParser xpp = ctx.getParser();

        if (xpp.getEventType() == XmlPullParser.START_DOCUMENT) {
            xpp.next();
        }

        this.doParseEventAttributes(ctx);
        // eliminated the symbol table and the exception call

        // Capture the start element name
        String startElementName = xpp.getName();

        while (xpp.next() != XmlPullParser.END_DOCUMENT) {

            if (xpp.getEventType() == XmlPullParser.END_TAG
                && xpp.getName() != null
                && xpp.getName().equals(startElementName)) {
                return this;
            }

            if (xpp.getEventType() == XmlPullParser.TEXT) {
                this.doAddCharacters(ctx);
            } else {
                this.doParseEventContent(ctx);
            }

        }

        return null;
    }

    protected void doParseEventAttributes(XmlPullParserContext ctx) {

        XmlPullParser xpp = ctx.getParser();

        int attributeCount = xpp.getAttributeCount();

        String attributeName;
        String attributeValue;
        for (int i = 0; i < attributeCount; i++) {
            attributeName = xpp.getAttributeName(i);
            attributeValue = xpp.getAttributeValue(i);
            this.setField(attributeName, attributeValue);
        }

    }

    protected void doAddCharacters(XmlPullParserContext ctx) {

        String s = ctx.getParser().getText();
        if (s == null || s.isEmpty()) {
            return;
        } else {
            s = s.replaceAll("\n", "").trim();
        }

        StringBuilder sb = (StringBuilder) this.getField(CHARACTERS_CONTENT);
        if (sb != null) {
            sb.append(s);
        } else {
            this.setField(CHARACTERS_CONTENT, new StringBuilder(s));
        }

    }

    protected void doParseEventContent(XmlPullParserContext ctx) throws XmlPullParserException, IOException {

        XmlPullParser xpp = ctx.getParser();

        // Override in subclass to parse an event's sub-elements.
        if (xpp.getEventType() == XmlPullParser.START_TAG) {

            QName qName = new QName(xpp.getNamespace(), xpp.getName());
            XmlElementModel model = ctx.getParsableModel(qName);

            if (model == null) {

                // TODO log parser not found

                model = ctx.getUnrecognizedElementModel();

                ctx.registerParsableModel(qName, model);
            }

            if (model != null) {
                Object o = model.read(ctx);
                if (o == null) {
                    return;
                } else {
                    this.doAddEventContent(o, ctx);
                }
            }

        }

    }

    protected void doAddEventContent(Object o, XmlPullParserContext ctx) {

        XmlPullParser xpp = ctx.getParser();

        this.setField(new QName(xpp.getNamespace(), xpp.getName()), o);

    }

    public void setParent(XmlElementModel parent) {
        this.parent = parent;
    }

    public XmlElementModel getParent() {
        return this.parent;
    }

    public void setField(QName keyName, Object value)
    {
        this.setField(keyName.getLocalPart(), value);
    }

    public void setField(String keyName, Object value)
    {
        if (this.fields == null)
            this.fields = new HashMap<>();

        this.fields.put(keyName, value);
    }

    public void setFields(Map<String, Object> newFields)
    {
        if (this.fields == null)
            this.fields = new HashMap<>();

        for (Map.Entry<String, Object> nf : newFields.entrySet())
        {
            this.setField(nf.getKey(), nf.getValue());
        }
    }

    public Object getField(QName keyName)
    {
        return this.fields != null ? this.getField(keyName.getLocalPart()) : null;
    }

    public Object getField(String keyName)
    {
        return this.fields != null ? this.fields.get(keyName) : null;
    }

    public boolean hasField(QName keyName)
    {
        return this.hasField(keyName.getLocalPart());
    }

    public boolean hasField(String keyName)
    {
        return this.fields != null && this.fields.containsKey(keyName);
    }

    public void removeField(String keyName)
    {
        if (this.fields != null)
            this.fields.remove(keyName);
    }

    public boolean hasFields()
    {
        return this.fields != null;
    }

    public Map<String, Object> getFields()
    {
        return this.fields;
    }

    public XmlElementModel newInstance() {

        return new XmlElementModel(this.getNamespaceUri());

    }

}
