/*
 * Copyright (c) 2016 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.util.xml;

public abstract class XmlModel {

    protected XmlModel parent;

    public XmlModel() {
    }

    public XmlModel getParent() {
        return this.parent;
    }

    public void setParent(XmlModel parent) {
        this.parent = parent;
    }

    protected void parseField(String keyName, Object value) {

    }

    protected void parseText(String text) {

    }
}
