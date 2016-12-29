/*
 * Copyright (c) 2016 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.ogc.wms;

import android.support.test.filters.SmallTest;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.List;
import java.util.Set;

import gov.nasa.worldwind.util.xml.XmlPullParserContext;

import static org.junit.Assert.assertEquals;

@RunWith(AndroidJUnit4.class)
@SmallTest
public class WmsLayerCapabilitiesTest {

    @Test
    public void testDcpType_ParseAndReadSampleWms() throws Exception {

        // Sample XML
        String xml = "<Layer xmlns=\"http://www.opengis.net/wms\">\n" +
            "            <Title>\n" +
            "                <![CDATA[Layers]]>\n" +
            "            </Title>\n" +
            "            <CRS>CRS:84</CRS>\n" +
            "            <CRS>EPSG:4326</CRS>\n" +
            "            <CRS>EPSG:3857</CRS>\n" +
            "            <!-- alias 3857 -->\n" +
            "            <CRS>EPSG:102100</CRS>\n" +
            "            <EX_GeographicBoundingBox>\n" +
            "                <westBoundLongitude>-179.999996</westBoundLongitude>\n" +
            "                <eastBoundLongitude>179.999996</eastBoundLongitude>\n" +
            "                <southBoundLatitude>-89.000000</southBoundLatitude>\n" +
            "                <northBoundLatitude>89.000000</northBoundLatitude>\n" +
            "            </EX_GeographicBoundingBox>\n" +
            "            <BoundingBox CRS=\"CRS:84\" minx=\"-179.999996\" miny=\"-89.000000\" maxx=\"179.999996\" maxy=\"89.000000\"/>\n" +
            "            <BoundingBox CRS=\"EPSG:4326\" minx=\"-89.000000\" miny=\"-179.999996\" maxx=\"89.000000\" maxy=\"179.999996\"/>\n" +
            "            <BoundingBox CRS=\"EPSG:3857\" minx=\"-20074053.901178\" miny=\"-30281451.060015\" maxx=\"20074053.888922\" maxy=\"30281451.072272\"/>\n" +
            "            <Layer queryable=\"1\">\n" +
            "                <Name>0</Name>\n" +
            "                <Title>\n" +
            "                    <![CDATA[USGS TNM Topo Base Map]]>\n" +
            "                </Title>\n" +
            "                <Abstract>\n" +
            "                    <![CDATA[See http://viewer.nationalmap.gov/help for assistance with The National Map viewer, services, or metadata.]]>\n" +
            "                </Abstract>\n" +
            "                <CRS>CRS:84</CRS>\n" +
            "                <CRS>EPSG:4326</CRS>\n" +
            "                <CRS>EPSG:3857</CRS>\n" +
            "                <!-- alias 3857 -->\n" +
            "                <CRS>EPSG:102100</CRS>\n" +
            "                <EX_GeographicBoundingBox>\n" +
            "                    <westBoundLongitude>-179.999996</westBoundLongitude>\n" +
            "                    <eastBoundLongitude>179.999996</eastBoundLongitude>\n" +
            "                    <southBoundLatitude>-89.000000</southBoundLatitude>\n" +
            "                    <northBoundLatitude>89.000000</northBoundLatitude>\n" +
            "                </EX_GeographicBoundingBox>\n" +
            "                <BoundingBox CRS=\"CRS:84\" minx=\"-179.999996\" miny=\"-89.000000\" maxx=\"179.999996\" maxy=\"89.000000\"/>\n" +
            "                <BoundingBox CRS=\"EPSG:4326\" minx=\"-89.000000\" miny=\"-179.999996\" maxx=\"89.000000\" maxy=\"179.999996\"/>\n" +
            "                <BoundingBox CRS=\"EPSG:3857\" minx=\"-20074053.901178\" miny=\"-30281451.060015\" maxx=\"20074053.888922\" maxy=\"30281451.072272\"/>\n" +
            "                <Style>\n" +
            "                    <Name>default</Name>\n" +
            "                    <Title>0</Title>\n" +
            "                    <LegendURL width=\"0\" height=\"0\">\n" +
            "                        <Format>image/png</Format>\n" +
            "                        <OnlineResource xlink:href=\"https://basemap.nationalmap.gov/arcgis/services/USGSTopo/MapServer/WmsServer?request=GetLegendGraphic%26version=1.3.0%26format=image/png%26layer=0\" xlink:type=\"simple\" xmlns:xlink=\"http://www.w3.org/1999/xlink\" />\n" +
            "                    </LegendURL>\n" +
            "                </Style>\n" +
            "                <MinScaleDenominator>17060.900298</MinScaleDenominator>\n" +
            "            </Layer>\n" +
            "        </Layer>";
        // Initialize the context and basic model
        XmlPullParserContext context = new XmlPullParserContext(XmlPullParserContext.DEFAULT_NAMESPACE);
        InputStream is = new ByteArrayInputStream(xml.getBytes());
        context.setParserInput(is);
        WmsLayerCapabilities elementModel = new WmsLayerCapabilities(XmlPullParserContext.DEFAULT_NAMESPACE);
        Object o;

        do {
            o = elementModel.read(context);
        } while (o != null);
        Set<WmsBoundingBox> boundingBoxes = elementModel.getBoundingBoxes();
        List<WmsLayerCapabilities> namedLayers = elementModel.getNamedLayers();

        assertEquals("test title", "Layers", elementModel.getTitle());
        assertEquals("test reference system count", 4, elementModel.getCRS().size());
        assertEquals("test bounding box count", 3, boundingBoxes.size());
        assertEquals("test named layer count", 1, namedLayers.size());
        assertEquals("test named layer name", "0", namedLayers.get(0).getName());
        // TODO add testing off all getters
    }
}
