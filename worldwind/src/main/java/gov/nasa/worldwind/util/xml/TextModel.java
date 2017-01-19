/*
 * Copyright (c) 2017 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.util.xml;

public class TextModel extends XmlModel {

    protected StringBuilder text;

    public TextModel() {
    }

    public String getValue() {
        return (this.text != null) ? this.text.toString() : null;
    }

    @Override
    protected void parseText(String text) {
        if (text == null || text.isEmpty()) {
            return; // nothing to parse
        }

        text = text.replaceAll("\n", "").trim();
        if (text.isEmpty()) {
            return; // nothing but whitespace
        }

        if (this.text == null) {
            this.text = new StringBuilder();
        }
        this.text.append(text);
    }
}
