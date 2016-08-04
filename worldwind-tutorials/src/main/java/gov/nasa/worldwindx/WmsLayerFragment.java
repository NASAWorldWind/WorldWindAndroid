/*
 * Copyright (c) 2016 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwindx;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import gov.nasa.worldwind.WorldWindow;
import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.layer.BackgroundLayer;
import gov.nasa.worldwind.layer.BlueMarbleLandsatLayer;
import gov.nasa.worldwind.ogc.WmsLayer;
import gov.nasa.worldwind.ogc.WmsLayerConfig;

public class WmsLayerFragment extends BasicGlobeFragment {

    /**
     * Creates a new WorldWindow (GLSurfaceView) object with a WMS Layer
     *
     * @return The WorldWindow object containing the globe.
     */
    @Override
    public WorldWindow createWorldWindow() {
        // Let the super class (BasicGlobeFragment) do the creation
        WorldWindow wwd = super.createWorldWindow();

        // Configure an OGC Web Map Service (WMS) layer to display the
        // sea surface temperature layer from NASA's Near Earth Observations WMS.
        WmsLayerConfig config = new WmsLayerConfig();
        config.serviceAddress = "http://neowms.sci.gsfc.nasa.gov/wms/wms";
        config.wmsVersion = "1.1.1"; // NEO server works best with WMS 1.1.1
        config.layerNames = "MOD_LSTD_CLIM_M"; // Land Surface Temperature
        WmsLayer layer = new WmsLayer(new Sector().setFullSphere(), 1e3, config); // 1km resolution

        // Add the WMS layer to the World Window.
        wwd.getLayers().addLayer(layer);

        return wwd;
    }
}
