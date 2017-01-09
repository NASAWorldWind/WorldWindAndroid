/*
 * Copyright (c) 2017 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.ogc.wms;

import gov.nasa.worldwind.util.xml.XmlModel;
import gov.nasa.worldwind.util.xml.XmlPullParserContext;

public class WmsFormat extends XmlModel {

    public WmsFormat(String namespaceUri) {
        super(namespaceUri);
    }

    @Override
    protected void doAddCharacters(XmlPullParserContext ctx) {

        String s = ctx.getParser().getText();
        if (s == null || s.isEmpty()) {
            return;
        } else {
            // The Format element represents a mime type and mime types are case insensitive. The common convention is
            // to use lowercase, which is what is applied here. Classes which may check the Format element
            // CHARACTER_CONTENTS should also utilize lowercase mime types.
            s = s.replaceAll("\n", "").trim().toLowerCase();
        }

        StringBuilder sb = (StringBuilder) this.getField(CHARACTERS_CONTENT);
        if (sb != null) {
            sb.append(s);
        } else {
            this.setField(CHARACTERS_CONTENT, new StringBuilder(s));
        }
    }
}
