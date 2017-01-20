/*
 * Copyright (c) 2017 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.ogc.wmts;

import android.util.Xml;

import org.xmlpull.v1.XmlPullParser;

import java.io.InputStream;
import java.util.LinkedHashSet;
import java.util.Set;

import gov.nasa.worldwind.util.Logger;
import gov.nasa.worldwind.util.xml.XmlModel;
import gov.nasa.worldwind.util.xml.XmlModelParser;

public class WmtsCapabilities extends XmlModel {

    protected OwsServiceIdentification serviceIdentification;

    protected OwsServiceProvider serviceProvider;

    protected OwsOperationsMetadata operationsMetadata;

    protected Set<WmtsLayer> layers = new LinkedHashSet<>();

    public static WmtsCapabilities getCapabilities(InputStream inputStream) throws Exception {
        XmlPullParser pullParser = Xml.newPullParser();
        pullParser.setInput(inputStream, null /*inputEncoding*/);

        XmlModelParser modelParser = new WmtsXmlParser();
        modelParser.setPullParser(pullParser);

        Object result = modelParser.parse();
        if (!(result instanceof WmtsCapabilities)) {
            throw new RuntimeException(
                Logger.logMessage(Logger.ERROR, "WmtsCapabilities", "getCapabilities", "Invalid WMTS Capabilities input"));
        }

        return (WmtsCapabilities) result;
    }

    @Override
    protected void parseField(String keyName, Object value) {
        if (keyName.equals("ServiceIdentification")) {
            this.serviceIdentification = (OwsServiceIdentification) value;
        } else if (keyName.equals("ServiceProvider")) {
            this.serviceProvider = (OwsServiceProvider) value;
        } else if (keyName.equals("OperationsMetadata")) {
            this.operationsMetadata = (OwsOperationsMetadata) value;
        } else if (keyName.equals("Layer")) {
            this.layers.add((WmtsLayer) value);
        } else if (keyName.equals("Contents")) {
            this.layers.addAll(((WmtsContents) value).layers);
        }
    }
}
