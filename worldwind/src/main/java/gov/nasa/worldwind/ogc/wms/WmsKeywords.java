/*
 * Copyright (c) 2016 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.ogc.wms;

import java.util.HashSet;
import java.util.Set;

import javax.xml.namespace.QName;

import gov.nasa.worldwind.util.xml.XmlModel;

public class WmsKeywords extends XmlModel {

    protected QName keywords;

    public WmsKeywords(String namespaceUri) {
        super(namespaceUri);
        this.initialize();
    }

    protected void initialize() {
        this.keywords = new QName(this.getNamespaceUri(), "Keyword");
    }

    public Set<String> getKeywords() {
        return (Set<String>) this.getField(this.keywords);
    }

    @Override
    public void setField(QName keyName, Object value) {

        if (keyName.equals(this.keywords)) {
            Set<String> keywords = (Set<String>) this.getField(this.keywords);
            if (keywords == null) {
                keywords = new HashSet<>();
                super.setField(this.keywords, keywords);
            }

            if (value instanceof XmlModel) {
                Object o = ((XmlModel) value).getField(XmlModel.CHARACTERS_CONTENT);
                if (o != null) {
                    keywords.add(o.toString());
                } else {
                    super.setField(keyName, value);
                }
            } else {
                super.setField(keyName, value);
            }
        } else {
            super.setField(keyName, value);
        }
    }
}
