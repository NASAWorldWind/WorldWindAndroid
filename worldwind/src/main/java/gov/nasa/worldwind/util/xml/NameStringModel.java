/*
 * Copyright (c) 2016 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.util.xml;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;

public class NameStringModel extends XmlModel {

    public NameStringModel(String namespaceUri) {
        super(namespaceUri);
    }

    @Override
    public Object read(XmlPullParserContext ctx) throws XmlPullParserException, IOException {

        String s = ctx.getParser().getName();
        if (s != null && !s.isEmpty()) {
            s = s.replaceAll("\n", "").trim();
        }

        StringBuilder sb = (StringBuilder) this.getField(CHARACTERS_CONTENT);
        if (sb != null) {
            sb.append(s);
        } else {
            this.setField(CHARACTERS_CONTENT, new StringBuilder(s));
        }

        return super.read(ctx);
    }
}
