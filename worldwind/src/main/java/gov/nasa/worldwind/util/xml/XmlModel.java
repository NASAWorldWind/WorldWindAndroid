/*
 * Copyright (c) 2016 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.util.xml;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;

import javax.xml.namespace.QName;

import gov.nasa.worldwind.util.Logger;

public abstract class XmlModel {

    protected XmlModel parent;

    protected StringBuilder characterContent;

    public XmlModel() {
    }

    public void setParent(XmlModel parent) {
        this.parent = parent;
    }

    public XmlModel getParent() {
        return this.parent;
    }

    public Object read(XmlPullParserContext ctx) throws XmlPullParserException, IOException {

        if (ctx == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "XmlModel", "parse", "missingContext"));
        }

        XmlPullParser xpp = ctx.getParser();

        if (xpp.getEventType() == XmlPullParser.START_DOCUMENT) {
            xpp.next();
        }

        this.doParseEventAttributes(ctx);

        // Capture the start element name
        String startElementName = xpp.getName();

        while (xpp.next() != XmlPullParser.END_DOCUMENT) {

            if (xpp.getEventType() == XmlPullParser.END_TAG
                && xpp.getName() != null
                && xpp.getName().equals(startElementName)) {
                if (this.characterContent != null) {
                    this.setText(this.characterContent.toString());
                }
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

    protected void doParseEventAttributes(XmlPullParserContext ctx) throws XmlPullParserException, IOException {

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

        if (this.characterContent == null) {
            this.characterContent = new StringBuilder(s);
        } else {
            this.characterContent.append(s);
        }
    }

    protected void doParseEventContent(XmlPullParserContext ctx) throws XmlPullParserException, IOException {

        XmlPullParser xpp = ctx.getParser();

        if (xpp.getEventType() == XmlPullParser.START_TAG) {

            QName qName = new QName(xpp.getNamespace(), xpp.getName());
            XmlModel model = ctx.createParsableModel(qName);

            if (model != null) {
                model.setParent(this);
                Object o = model.read(ctx);
                if (o == null) {
                    return;
                } else {
                    this.setField(xpp.getName(), o);
                }
            }

        }

    }

    protected void setText(String value) {

    }

    protected void setField(String keyName, Object value) {

    }

}
