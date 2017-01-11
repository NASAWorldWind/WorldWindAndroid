/*
 * Copyright (c) 2016 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.ogc.gpkg;

public class GpkgEntry {

    protected GeoPackage container;

    public GpkgEntry() {
    }

    public GeoPackage getContainer() {
        return container;
    }

    public void setContainer(GeoPackage container) {
        this.container = container;
    }
}
