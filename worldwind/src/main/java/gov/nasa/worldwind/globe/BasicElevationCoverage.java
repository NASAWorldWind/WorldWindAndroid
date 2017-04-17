/*
 * Copyright (c) 2017 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.globe;

import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.ogc.WmsElevationCoverage;

/**
 * Displays NASA's global elevation coverage at 10m resolution within the continental United States, 30m resolution in
 * all other continents, and 900m resolution on the ocean floor, all from an OGC Web Map Service (WMS). By default,
 * BasicElevationCoverage is configured to retrieve elevation coverage from the WMS at <a
 * href="https://worldwind26.arc.nasa.gov/wms?SERVICE=WMS&REQUEST=GetCapabilities">https://worldwind26.arc.nasa.gov/wms</a>.
 */
public class BasicElevationCoverage extends WmsElevationCoverage {

    /**
     * Constructs a global elevation coverage with the WMS at https://worldwind26.arc.nasa.gov/wms.
     */
    public BasicElevationCoverage() {
        this("https://worldwind26.arc.nasa.gov/elev");
    }

    /**
     * Constructs a global elevation coverage with the WMS at a specified address.
     *
     * @param serviceAddress a URL string specifying the WMS address
     *
     * @throws IllegalArgumentException If the service address is null
     */
    public BasicElevationCoverage(String serviceAddress) {
        super(new Sector().setFullSphere(), 13, serviceAddress, "GEBCO,aster_v2,USGS-NED");
    }
}
