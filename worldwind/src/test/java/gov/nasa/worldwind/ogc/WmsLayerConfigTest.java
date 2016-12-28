/*
 * Copyright (c) 2016 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.ogc;

import org.junit.Test;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;

public class WmsLayerConfigTest {

    @Test
    public void testConstructor_Default() {
        assertNotNull("instantiation", new WmsLayerConfig());
    }

    @Test
    public void testConstructor_TwoParameter() {

        String serviceAddress = "notionalAddress";
        String layerList = "layer1";

        WmsLayerConfig wmsLayerConfig = new WmsLayerConfig(serviceAddress, layerList);

        assertNotNull("instantiation", wmsLayerConfig);
        assertEquals("service address parameter", serviceAddress, wmsLayerConfig.serviceAddress);
        assertEquals("layer list", layerList, wmsLayerConfig.layerNames);
    }

    @Test
    public void testConstructor_AllParameters() {

        String serviceAddess = "notionalAddress";
        String wmsVersion = "1.2.0";
        String layerNames = "layer1";
        String styleNames = "style1";
        String coordinateSystem = WmsTileFactoryTest.SYSTEM_CRS84;
        String imageFormat = "type/name";
        boolean transparent = false;
        String time = "1600-ZULU";

        WmsLayerConfig wmsLayerConfig = new WmsLayerConfig(serviceAddess, wmsVersion, layerNames, styleNames,
            coordinateSystem, imageFormat, transparent, time);

        assertNotNull("instantiation", wmsLayerConfig);
        assertEquals("service address", serviceAddess, wmsLayerConfig.serviceAddress);
        assertEquals("wms version", wmsVersion, wmsLayerConfig.wmsVersion);
        assertEquals("layer names", layerNames, wmsLayerConfig.layerNames);
        assertEquals("style names", styleNames, wmsLayerConfig.styleNames);
        assertEquals("coordinate system", coordinateSystem, wmsLayerConfig.coordinateSystem);
        assertEquals("image format", imageFormat, wmsLayerConfig.imageFormat);
        assertEquals("transparency", transparent, wmsLayerConfig.transparent);
        assertEquals("time", time, wmsLayerConfig.timeString);
    }
}
