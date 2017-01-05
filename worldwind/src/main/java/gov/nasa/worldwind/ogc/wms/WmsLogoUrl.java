/*
 * Copyright (c) 2016 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.ogc.wms;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.xml.namespace.QName;

import gov.nasa.worldwind.util.xml.XmlModel;

public class WmsLogoUrl extends XmlModel {

    protected QName format;

    protected QName onlineResource;

    protected QName width = new QName("", "width");

    protected QName height = new QName("", "height");

    public WmsLogoUrl(String namespaceURI) {
        super(namespaceURI);
        this.initialize();
    }

    protected void initialize() {
        this.format = new QName(this.getNamespaceUri(), "Format");
        this.onlineResource = new QName(this.getNamespaceUri(), "OnlineResource");
    }

    @Override
    public void setField(QName keyName, Object value) {

        if (keyName.equals(this.format)) {
            Set<String> formats = (Set<String>) this.getField(this.format);
            if (formats == null) {
                formats = new HashSet<>();
                super.setField(this.format, formats);
            }

            if (value instanceof XmlModel) {
                formats.add(((XmlModel) value).getCharactersContent());
                return;
            }
        }

        super.setField(keyName, value);
    }

    public Integer getWidth() {
        return this.getIntegerAttributeValue(this.width, false);
    }

    public Integer getHeight() {
        return this.getIntegerAttributeValue(this.height, false);
    }

    public Set<String> getFormats() {
        Object o = this.getField(this.format);
        if (o instanceof Set) {
            return (Set<String>) o;
        } else {
            return Collections.emptySet();
        }
    }

    public WmsOnlineResource getOnlineResource() {
        Object o = this.getField(this.onlineResource);
        if (o instanceof WmsOnlineResource) {
            return (WmsOnlineResource) o;
        } else {
            return null;
        }
    }
}
