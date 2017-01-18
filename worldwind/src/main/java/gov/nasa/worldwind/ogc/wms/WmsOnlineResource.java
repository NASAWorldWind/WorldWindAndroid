/*
 * Copyright (c) 2017 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.ogc.wms;

import gov.nasa.worldwind.util.xml.XmlModel;
import gov.nasa.worldwind.util.xml.XmlPullParserContext;

public class WmsOnlineResource extends XmlModel
{

    protected String type;

    protected String href;

    public WmsOnlineResource(String namespaceURI)
    {
        super(namespaceURI == null ? XmlPullParserContext.DEFAULT_NAMESPACE : namespaceURI);
    }

    public String getType()
    {
        return this.type;
    }

    public String getHref()
    {
        return this.href;
    }

    @Override
    public void setField(String keyName, Object value) {
        if (keyName.equals("type")) {
            this.type = value.toString();
        } else if (keyName.equals("href")) {
            this.href = value.toString();
        }
    }
}
