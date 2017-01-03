/*
 * Copyright (c) 2016 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.util.xml;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;

/**
 * The NameStringModel grabs the tag name and stores it in the {@link XmlModel#CHARACTERS_CONTENT} key of the fields
 * map. Elements that use this model are providing information based on their tag name instead of the more common text
 * portion or attribute. Specifically, we see this in the DCPType element of WMS Capabilities Documents.</p>
 * <p>
 * {@code <DCPType> <HTTP> <Get> ... </Get> <Post> ... </Post> ... }
 * <p>
 * Where {@code Get} and {@code Post} are providing contextual information we need but are only identifiable in our map
 * via the parent node.</p>
 * <p>
 * This model only adds the tag name to the CHARACTERS_CONTENT StringBuilder object stored in the map for easy
 * retrieval. After storing the name, it calls the super method to ensure proper walking/reading of the XML document.
 * </p>
 * <p>
 * This model should should not be used as the root model or when a text portion is enclosed by the element.
 */
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
