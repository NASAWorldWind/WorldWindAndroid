/*
 * Copyright (c) 2017 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.ogc.wmts;

import gov.nasa.worldwind.util.xml.XmlModelParser;

public class WmtsXmlParser extends XmlModelParser {

    protected String owsNamespace = "http://www.opengis.net/ows/1.1";

    protected String wmtsNamespace = "http://www.opengis.net/wmts/1.0";

    protected String xlinkNamespace = "http://www.w3.org/1999/xlink";

    public WmtsXmlParser() {
        this.registerParsers();
    }

    protected void registerParsers() {
        this.registerWmtsXmlModels();
        this.registerWmtsTextModels();
    }

    protected void registerWmtsXmlModels() {

        this.registerXmlModel(owsNamespace, "Address", OwsAddress.class);

        this.registerXmlModel(owsNamespace, "BoundingBox", OwsBoundingBox.class);

        this.registerXmlModel(wmtsNamespace, "Capabilities", WmtsCapabilities.class);
        this.registerXmlModel(owsNamespace, "ContactInfo", OwsContactInfo.class);
        this.registerXmlModel(wmtsNamespace, "Contents", WmtsContents.class);

        this.registerXmlModel(owsNamespace, "DCP", OwsDcp.class);

        this.registerXmlModel(owsNamespace, "Get", WmtsElementLink.class);

        this.registerXmlModel(owsNamespace, "HTTP", OwsHttp.class);

        this.registerXmlModel(owsNamespace, "Keywords", OwsKeywords.class);

        this.registerXmlModel(wmtsNamespace, "Layer", WmtsLayer.class);
        this.registerXmlModel(wmtsNamespace, "LegendURL", WmtsElementLink.class);

        this.registerXmlModel(owsNamespace, "Metadata", WmtsElementLink.class);

        this.registerXmlModel(owsNamespace, "Operation", OwsOperation.class);
        this.registerXmlModel(owsNamespace, "OperationsMetadata", OwsOperationsMetadata.class);


        this.registerXmlModel(owsNamespace, "Phone", OwsPhone.class);
        this.registerXmlModel(owsNamespace, "Post", WmtsElementLink.class);
        this.registerXmlModel(owsNamespace, "ProviderSite", WmtsElementLink.class);

        this.registerXmlModel(wmtsNamespace, "ResourceURL", WmtsResourceUrl.class);

        this.registerXmlModel(owsNamespace, "ServiceContact", OwsServiceContact.class);
        this.registerXmlModel(owsNamespace, "ServiceIdentification", OwsServiceIdentification.class);
        this.registerXmlModel(wmtsNamespace, "ServiceMetadataURL", WmtsElementLink.class);
        this.registerXmlModel(owsNamespace, "ServiceProvider", OwsServiceProvider.class);
        this.registerXmlModel(wmtsNamespace, "Style", WmtsStyle.class);

        this.registerXmlModel(wmtsNamespace, "Theme", WmtsTheme.class);
        this.registerXmlModel(wmtsNamespace, "Themes", WmtsThemes.class);
        this.registerXmlModel(wmtsNamespace, "TileMatrix", WmtsTileMatrix.class);
        this.registerXmlModel(wmtsNamespace, "TileMatrixSet", WmtsTileMatrixSet.class);
        this.registerXmlModel(wmtsNamespace, "TileMatrixSetLink", WmtsTileMatrixSetLink.class);

        this.registerXmlModel(owsNamespace, "WGS84BoundingBox", OwsBoundingBox.class);
    }

    protected void registerWmtsTextModels() {

        this.registerTxtModel(owsNamespace, "Abstract");
        this.registerTxtModel(owsNamespace, "AccessConstraints");
        this.registerTxtModel(owsNamespace, "AdministrativeArea");

        this.registerTxtModel(owsNamespace, "City");
        this.registerTxtModel(owsNamespace, "Country");

        this.registerTxtModel(owsNamespace, "DeliveryPoint");

        this.registerTxtModel(owsNamespace, "ElectronicMailAddress");

        this.registerTxtModel(owsNamespace, "Facsimile");
        this.registerTxtModel(owsNamespace, "Fees");
        this.registerTxtModel(wmtsNamespace, "Format");

        this.registerTxtModel(owsNamespace, "Identifier");
        this.registerTxtModel(owsNamespace, "IndividualName");
        this.registerTxtModel(wmtsNamespace, "InfoFormat");

        this.registerTxtModel(owsNamespace, "Keyword");

        this.registerTxtModel(wmtsNamespace, "LayerRef");
        this.registerTxtModel(owsNamespace, "LowerCorner");

        this.registerTxtModel(wmtsNamespace, "MatrixHeight");
        this.registerTxtModel(wmtsNamespace, "MatrixWidth");

        this.registerTxtModel(owsNamespace, "PositionName");
        this.registerTxtModel(owsNamespace, "PostalCode");
        this.registerTxtModel(owsNamespace, "ProviderName");

        this.registerTxtModel(wmtsNamespace, "ScaleDenominator");
        this.registerTxtModel(owsNamespace, "ServiceType");
        this.registerTxtModel(owsNamespace, "ServiceTypeVersion");
        this.registerTxtModel(owsNamespace, "SupportedCRS");

        this.registerTxtModel(wmtsNamespace, "TileHeight");
        this.registerTxtModel(wmtsNamespace, "TileWidth");
        this.registerTxtModel(owsNamespace, "Title");
        this.registerTxtModel(wmtsNamespace, "TopLeftCorner");

        this.registerTxtModel(owsNamespace, "UpperCorner");

        this.registerTxtModel(owsNamespace, "Voice");

        this.registerTxtModel(wmtsNamespace, "WellKnownScaleSet");
    }
}
