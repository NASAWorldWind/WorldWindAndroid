/*
 * Copyright (c) 2017 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.ogc.wmts;

import android.util.Xml;

import org.xmlpull.v1.XmlPullParser;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import gov.nasa.worldwind.util.Logger;
import gov.nasa.worldwind.util.xml.XmlModel;
import gov.nasa.worldwind.util.xml.XmlModelParser;

public class WmtsCapabilities extends XmlModel {

    protected String version;

    protected String updateSequence;

    protected OwsServiceIdentification serviceIdentification;

    protected OwsServiceProvider serviceProvider;

    protected OwsOperationsMetadata operationsMetadata;

    protected WmtsContents contents;

    protected final List<WmtsTheme> themes = new ArrayList<>();

    protected final List<WmtsElementLink> serviceMetadataUrls = new ArrayList<>();

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

    public String getVersion() {
        return this.version;
    }

    public String getUpdateSequence() {
        return this.updateSequence;
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

    public WmtsContents getContents() {
        return this.contents;
    }

    public List<WmtsTheme> getThemes() {
        return this.themes;
    }

    public List<WmtsElementLink> getServiceMetadataUrls() {
        return this.serviceMetadataUrls;
    }

    public List<WmtsLayer> getLayers() {
        return this.getContents().getLayers();
    }

    public WmtsLayer getLayer(String identifier) {
        for (WmtsLayer layer : this.getContents().getLayers()) {
            if (layer.getIdentifier().equals(identifier)) {
                return layer;
            }
        }

        return null;
    }

    public List<WmtsTileMatrixSet> getTileMatrixSets() {
        return this.getContents().getTileMatrixSets();
    }

    public WmtsTileMatrixSet getTileMatrixSet(String identifier) {
        for (WmtsTileMatrixSet tileMatrixSet : this.getContents().getTileMatrixSets()) {
            if (tileMatrixSet.getIdentifier().equals(identifier)) {
                return tileMatrixSet;
            }
        }

        return null;
    }

    @Override
    protected void parseField(String keyName, Object value) {
        switch (keyName) {
            case "ServiceIdentification":
                this.serviceIdentification = (OwsServiceIdentification) value;
                break;
            case "ServiceProvider":
                this.serviceProvider = (OwsServiceProvider) value;
                break;
            case "OperationsMetadata":
                this.operationsMetadata = (OwsOperationsMetadata) value;
                break;
            case "Contents":
                this.contents = (WmtsContents) value;
                break;
            case "Themes":
                this.themes.addAll(((WmtsThemes) value).themes);
                break;
            case "ServiceMetadataURL":
                this.serviceMetadataUrls.add((WmtsElementLink) value);
                break;
            case "version":
                this.version = (String) value;
                break;
            case "updateSequence":
                this.updateSequence = (String) value;
                break;
        }
    }
}
