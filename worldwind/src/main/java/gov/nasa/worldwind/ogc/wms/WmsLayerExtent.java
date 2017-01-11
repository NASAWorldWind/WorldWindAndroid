/*
 * Copyright (c) 2016 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.ogc.wms;

import android.util.Log;

import javax.xml.namespace.QName;

import gov.nasa.worldwind.util.xml.XmlModel;

public class WmsLayerExtent extends XmlModel {

    public static final String DEFAULT_ATTRIBUTE_NAMESPACE = "";

    public static final QName NAME = new QName(DEFAULT_ATTRIBUTE_NAMESPACE, "name");

    public static final QName DEFAULT = new QName(DEFAULT_ATTRIBUTE_NAMESPACE, "default");

    public static final QName MULTIPLE_VALUES = new QName(DEFAULT_ATTRIBUTE_NAMESPACE, "multipleValues");

    public static final QName NEAREST_VALUES = new QName(DEFAULT_ATTRIBUTE_NAMESPACE, "nearestValues");

    public static final QName CURRENT = new QName(DEFAULT_ATTRIBUTE_NAMESPACE, "current");

    public WmsLayerExtent(String namespaceURI) {
        super(namespaceURI);
    }

//    @Override
//    protected void doParseEventAttributes(XmlEventParserContext ctx, XmlEvent event, Object... args)
//    {
//        Iterator iter = event.asStartElement().getAttributes();
//        if (iter == null)
//            return;
//
//        while (iter.hasNext())
//        {
//            Attribute attr = (Attribute) iter.next();
//            if (attr.getName().getLocalPart().equals("name") && attr.getValue() != null)
//                this.setName(attr.getValue());
//
//            else if (attr.getName().getLocalPart().equals("default") && attr.getValue() != null)
//                this.setDefaultValue(attr.getValue());
//
//            else if (attr.getName().getLocalPart().equals("nearestValue") && attr.getValue() != null)
//            {
//                Boolean d = WWUtil.convertStringToBoolean(attr.getValue());
//                if (d != null)
//                    this.setNearestValue(d);
//            }
//        }
//    }

    public String getExtent() {
        return this.getField(XmlModel.CHARACTERS_CONTENT).toString();
    }

    public String getName() {
        return this.getField(NAME) != null ? this.getField(NAME).toString() : null;
    }

    protected void setName(String name) {
        this.setField(NAME, name);
    }

    public String getDefaultValue() {
        return this.getField(DEFAULT) != null ? this.getField(DEFAULT).toString() : null;
    }

    protected void setDefaultValue(String defaultValue) {
        this.setField(DEFAULT, defaultValue);
    }

    public Boolean isNearestValue() {
        String value = this.getField(NEAREST_VALUES) != null ? this.getField(NEAREST_VALUES).toString() : null;
        if (value == null) {
            return null;
        }

        try {
            int numericalValue = Integer.parseInt(value);
            if (numericalValue == 1) {
                return true;
            } else {
                return false;
            }
        } catch (NumberFormatException e) {
            Log.d("gov.nasa.worldwind", e.toString());
        }
        return null;
    }

    protected void setNearestValue(Boolean nearestValue) {
        this.setField(NEAREST_VALUES, "1");
    }
}
