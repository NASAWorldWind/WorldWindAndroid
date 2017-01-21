/*
 * Copyright (c) 2017 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.ogc.wmts;

import android.util.Xml;

import org.xmlpull.v1.XmlPullParser;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import gov.nasa.worldwind.util.Logger;
import gov.nasa.worldwind.util.xml.XmlModel;
import gov.nasa.worldwind.util.xml.XmlModelParser;

public class WmtsCapabilities extends XmlModel {

    protected OwsServiceIdentification serviceIdentification;

    protected OwsServiceProvider serviceProvider;

    protected OwsOperationsMetadata operationsMetadata;

    protected List<WmtsLayer> layers = new ArrayList<>();

    protected Map<String, WmtsTileMatrixSet> matrixSetMap;

    protected List<WmtsTheme> themes = new ArrayList<>();

    protected List<WmtsElementLink> serviceMetadataUrls = new ArrayList<>();

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

    public OwsOperationsMetadata getOperationsMetadata() {
        return this.operationsMetadata;
    }

    public OwsServiceProvider getServiceProvider() {
        return this.serviceProvider;
    }

    public OwsServiceIdentification getServiceIdentification() {
        return this.serviceIdentification;
    }

    public List<WmtsLayer> getLayers() {
        return Collections.unmodifiableList(this.layers);
    }

    public WmtsTileMatrixSet getTileMatrixSet(String identifier) {
        return this.matrixSetMap.get(identifier);
    }

    public List<WmtsTileMatrixSet> getTileMatrixSets() {
        List<WmtsTileMatrixSet> tileMatrixSets = new ArrayList<>();
        for (Map.Entry<String, WmtsTileMatrixSet> tileMatrixSet : this.matrixSetMap.entrySet()) {
            tileMatrixSets.add(tileMatrixSet.getValue());
        }
        return tileMatrixSets;
    }

    public List<WmtsTheme> getThemes() {
        return Collections.unmodifiableList(this.themes);
    }

    public List<WmtsElementLink> getServiceMetadataUrls() {
        return Collections.unmodifiableList(this.serviceMetadataUrls);
    }

    @Override
    protected void parseField(String keyName, Object value) {
        if (keyName.equals("ServiceIdentification")) {
            this.serviceIdentification = (OwsServiceIdentification) value;
        } else if (keyName.equals("ServiceProvider")) {
            this.serviceProvider = (OwsServiceProvider) value;
        } else if (keyName.equals("OperationsMetadata")) {
            this.operationsMetadata = (OwsOperationsMetadata) value;
        } else if (keyName.equals("Contents")) {
            WmtsContents wmtsContents = (WmtsContents) value;
            this.layers.addAll(wmtsContents.layers);
            this.matrixSetMap = wmtsContents.matrixSetMap;
        } else if (keyName.equals("Themes")) {
            this.themes.addAll(((WmtsThemes) value).themes);
        } else if (keyName.equals("ServiceMetadataURL")) {
            this.serviceMetadataUrls.add((WmtsElementLink) value);
        }
    }
}
