/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwind.layer.graticule;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import gov.nasa.worldwind.geom.Location;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.render.RenderContext;

abstract class AbstractLatLonGraticuleLayer extends AbstractGraticuleLayer implements GridTilesSupport.Callback  {

    public enum AngleFormat {
        DD, DM, DMS
    }

    private final GridTilesSupport gridTilesSupport;
    private final List<Double> latitudeLabels = new ArrayList<>();
    private final List<Double> longitudeLabels = new ArrayList<>();
    private AngleFormat angleFormat = AngleFormat.DMS;

    AbstractLatLonGraticuleLayer(String name) {
        super(name);
        this.gridTilesSupport = new GridTilesSupport(this, 18, 36);
    }

    /**
     * Get the graticule division and angular display format. Can be one of {@link AngleFormat#DD}
     * or {@link AngleFormat#DMS}.
     *
     * @return the graticule division and angular display format.
     */
    public AngleFormat getAngleFormat() {
        return this.angleFormat;
    }

    /**
     * Sets the graticule division and angular display format. Can be one of {@link AngleFormat#DD},
     * {@link AngleFormat#DMS} of {@link AngleFormat#DM}.
     *
     * @param format the graticule division and angular display format.
     */
    public void setAngleFormat(AngleFormat format) {
        if (this.angleFormat.equals(format))
            return;

        this.angleFormat = format;
        this.gridTilesSupport.clearTiles();
    }

    @Override
    protected void clear(RenderContext rc) {
        super.clear(rc);
        this.latitudeLabels.clear();
        this.longitudeLabels.clear();
    }

    @Override
    protected void selectRenderables(RenderContext rc) {
        this.gridTilesSupport.selectRenderables(rc);
    }

    @Override
    public Sector getGridSector(int row, int col) {
        int minLat = -90 + row * 10;
        int maxLat = minLat + 10;
        int minLon = -180 + col * 10;
        int maxLon = minLon + 10;
        return Sector.fromDegrees(minLat, minLon, maxLat - minLat, maxLon - minLon);
    }

    @Override
    public int getGridColumn(double longitude) {
        return Math.min((int) Math.floor((longitude + 180) / 10d), 35);
    }

    @Override
    public int getGridRow(double latitude) {
        return Math.min((int) Math.floor((latitude + 90) / 10d), 17);
    }

    void addLabel(double value, String labelType, String graticuleType, double resolution, Location labelOffset) {
        Position position = null;
        if (labelType.equals(GridElement.TYPE_LATITUDE_LABEL)) {
            if (!this.latitudeLabels.contains(value)) {
                this.latitudeLabels.add(value);
                position = Position.fromDegrees(value, labelOffset.longitude, 0);
            }
        } else if (labelType.equals(GridElement.TYPE_LONGITUDE_LABEL)) {
            if (!this.longitudeLabels.contains(value)) {
                this.longitudeLabels.add(value);
                position = Position.fromDegrees(labelOffset.latitude, value, 0);
            }
        }
        if (position != null) {
            String label = makeAngleLabel(value, resolution);
            this.addRenderable(this.createTextRenderable(position, label, resolution), graticuleType);
        }
    }

    private String toDecimalDegreesString(double angle, int digits) {
        return String.format("%." + digits + "f\u00B0", angle);
    }

    private String toDMSString(double angle) {
        int sign = (int) Math.signum(angle);
        angle *= sign;
        int d = (int) Math.floor(angle);
        angle = (angle - d) * 60d;
        int m = (int) Math.floor(angle);
        angle = (angle - m) * 60d;
        int s = (int) Math.round(angle);

        if (s == 60) {
            m++;
            s = 0;
        }
        if (m == 60) {
            d++;
            m = 0;
        }

        return (sign == -1 ? "-" : "") + d + '\u00B0' + ' ' + m + '\u2019' + ' ' + s + '\u201d';
    }

    private String toDMString(double angle) {
        int sign = (int) Math.signum(angle);
        angle *= sign;
        int d = (int) Math.floor(angle);
        angle = (angle - d) * 60d;
        int m = (int) Math.floor(angle);
        angle = (angle - m) * 60d;
        int s = (int) Math.round(angle);

        if (s == 60) {
            m++;
            s = 0;
        }
        if (m == 60) {
            d++;
            m = 0;
        }

        double mf = s == 0 ? m : m + s / 60.0;

        return (sign == -1 ? "-" : "") + d + '\u00B0' + ' ' + String.format(Locale.getDefault(), "%5.2f", mf) + '\u2019';
    }

    private double[] toDMS(double angle) {
        int sign = (int) Math.signum(angle);

        angle *= sign;
        int d = (int) Math.floor(angle);
        angle = (angle - d) * 60d;
        int m = (int) Math.floor(angle);
        angle = (angle - m) * 60d;
        double s = Math.rint(angle * 100) / 100;  // keep two decimals for seconds

        if (s == 60) {
            m++;
            s = 0;
        }
        if (m == 60) {
            d++;
            m = 0;
        }

        return new double[] {sign * d, m, s};
    }

    private String makeAngleLabel(double angle, double resolution) {
        double epsilon = .000000001;
        String label;
        if (this.getAngleFormat().equals(AngleFormat.DMS)) {
            if (resolution >= 1)
                label = toDecimalDegreesString(angle, 0);
            else {
                double[] dms = toDMS(angle);
                if (dms[1] < epsilon && dms[2] < epsilon)
                    label = String.format(Locale.getDefault(), "%4d\u00B0", (int) dms[0]);
                else if (dms[2] < epsilon)
                    label = String.format(Locale.getDefault(), "%4d\u00B0 %2d\u2019", (int) dms[0], (int) dms[1]);
                else
                    label = toDMSString(angle);
            }
        } else if (this.getAngleFormat().equals(AngleFormat.DM)) {
            if (resolution >= 1)
                label = toDecimalDegreesString(angle,0);
            else {
                double[] dms = toDMS(angle);
                if (dms[1] < epsilon && dms[2] < epsilon)
                    label = String.format(Locale.getDefault(), "%4d\u00B0", (int) dms[0]);
                else if (dms[2] < epsilon)
                    label = String.format(Locale.getDefault(), "%4d\u00B0 %2d\u2019", (int) dms[0], (int) dms[1]);
                else
                    label = toDMString(angle);
            }
        } else { // default to decimal degrees
            if (resolution >= 1)
                label = toDecimalDegreesString(angle, 0);
            else if (resolution >= .1)
                label = toDecimalDegreesString(angle, 1);
            else if (resolution >= .01)
                label = toDecimalDegreesString(angle, 2);
            else if (resolution >= .001)
                label = toDecimalDegreesString(angle, 3);
            else
                label = toDecimalDegreesString(angle, 4);
        }

        return label;
    }

}
