/*
 * Copyright (c) 2016 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.globe;

import java.util.ArrayList;
import java.util.List;

import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.geom.Vec3;
import gov.nasa.worldwind.util.Logger;

public class BasicTerrain implements Terrain {

    protected List<TerrainTile> tiles = new ArrayList<>();

    protected Sector sector = new Sector();

    public BasicTerrain() {
    }

    public void addTile(TerrainTile tile) {
        if (tile == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "BasicTerrain", "addTile", "missingTile"));
        }

        this.tiles.add(tile);
        this.sector.union(tile.sector);
    }

    public void clearTiles() {
        this.tiles.clear();
        this.sector.setEmpty();
    }

    @Override
    public Sector getSector() {
        return this.sector;
    }

    @Override
    public Vec3 geographicToCartesian(double latitude, double longitude, double altitude, @WorldWind.AltitudeMode int altitudeMode, Vec3 result) {
        if (result == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "BasicTerrain", "geographicToCartesian", "missingResult"));
        }

        return null; // TODO
    }
}
