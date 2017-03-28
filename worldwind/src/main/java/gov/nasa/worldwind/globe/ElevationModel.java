/*
 * Copyright (c) 2017 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.globe;

import java.util.ArrayList;
import java.util.Iterator;

import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.util.Logger;
import gov.nasa.worldwind.util.Tile;

public class ElevationModel implements Iterable<ElevationCoverage> {

    protected ArrayList<ElevationCoverage> coverages = new ArrayList<>();

    private float[] coverageLimits = new float[2];

    public ElevationModel() {
    }

    public ElevationModel(ElevationModel model) {
        if (model == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "ElevationModel", "constructor", "missingModel"));
        }

        this.addAllCoverages(model);
    }

    public ElevationModel(Iterable<? extends ElevationCoverage> iterable) {
        if (iterable == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "ElevationModel", "constructor", "missingIterable"));
        }

        for (ElevationCoverage coverage : iterable) {
            if (coverage == null) {
                throw new IllegalArgumentException(
                    Logger.logMessage(Logger.ERROR, "ElevationModel", "constructor", "missingCoverage"));
            }

            this.addCoverage(coverage);
        }
    }

    public int count() {
        return this.coverages.size();
    }

    public ElevationCoverage getCoverageNamed(String name) {

        for (int idx = 0, len = this.coverages.size(); idx < len; idx++) {
            ElevationCoverage coverage = this.coverages.get(idx);
            String coverageName = coverage.getDisplayName();
            if ((coverageName == null) ? (name == null) : coverageName.equals(name)) {
                return coverage;
            }
        }

        return null;
    }

    public ElevationCoverage getCoverageWithProperty(Object key, Object value) {

        for (int idx = 0, len = this.coverages.size(); idx < len; idx++) {
            ElevationCoverage coverage = this.coverages.get(idx);
            if (coverage.hasUserProperty(key)) {
                Object coverageValue = coverage.getUserProperty(key);
                if ((coverageValue == null) ? (value == null) : coverageValue.equals(value)) {
                    return coverage;
                }
            }
        }

        return null;
    }

    public boolean addCoverage(ElevationCoverage coverage) {
        if (coverage == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "ElevationModel", "addCoverage", "missingCoverage"));
        }

        return !this.coverages.contains(coverage) && this.coverages.add(coverage);
    }

    public boolean addAllCoverages(ElevationModel model) {
        if (model == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "ElevationModel", "addAllCoverages", "missingModel"));
        }

        ArrayList<ElevationCoverage> thisList = this.coverages;
        ArrayList<ElevationCoverage> thatList = model.coverages;
        thisList.ensureCapacity(thatList.size());
        boolean changed = false;

        for (int idx = 0, len = thatList.size(); idx < len; idx++) {
            ElevationCoverage thatCoverage = thatList.get(idx); // we know the contents of model.coverages is valid
            changed |= this.addCoverage(thatCoverage);
        }

        return changed;
    }

    public boolean removeCoverage(ElevationCoverage coverage) {
        if (coverage == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "ElevationModel", "removeCoverage", "missingCoverage"));
        }

        return this.coverages.remove(coverage);
    }

    public boolean removeAllCoverages(ElevationModel model) {
        if (model == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "ElevationModel", "removeAllCoverages", "missingModel"));
        }

        return this.coverages.removeAll(model.coverages);
    }

    public void clearCoverages() {
        this.coverages.clear();
    }

    @Override
    public Iterator<ElevationCoverage> iterator() {
        return this.coverages.iterator();
    }

    public boolean hasCoverage(Sector sector) {
        if (sector == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "ElevationModel", "hasCoverage", "missingSector"));
        }

        for (int idx = 0, len = this.coverages.size(); idx < len; idx++) {
            ElevationCoverage coverage = this.coverages.get(idx);
            if (coverage.hasCoverage(sector)) {
                return true;
            }
        }

        return false;
    }

    public long getTimestamp() {
        long maxTimestamp = 0;

        for (int idx = 0, len = this.coverages.size(); idx < len; idx++) {
            ElevationCoverage coverage = this.coverages.get(idx);
            long timestamp = coverage.getTimestamp();
            if (maxTimestamp < timestamp) {
                maxTimestamp = timestamp;
            }
        }

        return maxTimestamp;
    }

    public float getHeight(double latitude, double longitude) {
        for (int idx = this.coverages.size() - 1; idx >= 0; idx--) {
            ElevationCoverage coverage = this.coverages.get(idx);
            float height = coverage.getHeight(latitude, longitude);
            if (!Float.isNaN(height)) {
                return height;
            }
        }

        return Float.NaN;
    }

    public float[] getHeight(Tile tile, float[] result) {
        if (tile == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "ElevationModel", "getHeight", "missingTile"));
        }

        if (result == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "ElevationModel", "getHeight", "missingResult"));
        }

        for (int idx = 0, len = this.coverages.size(); idx < len; idx++) {
            ElevationCoverage coverage = this.coverages.get(idx);
            coverage.getHeight(tile, result);
        }

        return result;
    }

    public float[] getHeightLimits(Tile tile, float[] result) {
        if (tile == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "ElevationModel", "getHeightLimits", "missingTile"));
        }

        if (result == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "ElevationModel", "getHeightLimits", "missingResult"));
        }

        for (int idx = 0, len = this.coverages.size(); idx < len; idx++) {
            ElevationCoverage coverage = this.coverages.get(idx);
            coverage.getHeightLimits(tile, this.coverageLimits);

            if (result[0] > this.coverageLimits[0]) {
                result[0] = this.coverageLimits[0];
            }

            if (result[1] < this.coverageLimits[1]) {
                result[1] = this.coverageLimits[1];
            }
        }

        return result;
    }
}
