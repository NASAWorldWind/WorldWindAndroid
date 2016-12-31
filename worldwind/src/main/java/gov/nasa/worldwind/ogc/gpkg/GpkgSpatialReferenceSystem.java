/*
 * Copyright (c) 2016 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.ogc.gpkg;

public class GpkgSpatialReferenceSystem extends GpkgEntry {

    protected String srsName;

    protected int srsId;

    protected String organization;

    protected int organizationCoordSysId;

    protected String definition;

    protected String description;

    public GpkgSpatialReferenceSystem() {
    }

    public String getSrsName() {
        return srsName;
    }

    public void setSrsName(String text) {
        this.srsName = text;
    }

    public int getSrsId() {
        return srsId;
    }

    public void setSrsId(int id) {
        this.srsId = id;
    }

    public String getOrganization() {
        return organization;
    }

    public void setOrganization(String text) {
        this.organization = text;
    }

    public int getOrganizationCoordSysId() {
        return organizationCoordSysId;
    }

    public void setOrganizationCoordSysId(int id) {
        this.organizationCoordSysId = id;
    }

    public String getDefinition() {
        return definition;
    }

    public void setDefinition(String wktString) {
        this.definition = wktString;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String text) {
        this.description = text;
    }
}
