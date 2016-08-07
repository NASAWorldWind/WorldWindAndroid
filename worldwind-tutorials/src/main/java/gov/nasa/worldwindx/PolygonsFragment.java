/*
 * Copyright (c) 2016 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwindx;

import java.util.Arrays;
import java.util.List;

import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.WorldWindow;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.layer.RenderableLayer;
import gov.nasa.worldwind.render.Color;
import gov.nasa.worldwind.shape.Polygon;
import gov.nasa.worldwind.shape.ShapeAttributes;

public class PolygonsFragment extends BasicGlobeFragment {

    /**
     * Creates a new WorldWindow (GLSurfaceView) object with a set of Polygon shapes
     *
     * @return The WorldWindow object containing the globe.
     */
    @Override
    public WorldWindow createWorldWindow() {
        // Let the super class (BasicGlobeFragment) do the creation
        WorldWindow wwd = super.createWorldWindow();

        // Create a layer to display the tutorial polygons.
        RenderableLayer layer = new RenderableLayer();
        wwd.getLayers().addLayer(layer);

        // Create a basic polygon with the default attributes, the default altitude mode (ABSOLUTE),
        // and the default path type (GREAT_CIRCLE).
        List<Position> positions = Arrays.asList(
            Position.fromDegrees(40, -135, 5.0e5),
            Position.fromDegrees(45, -140, 7.0e5),
            Position.fromDegrees(50, -130, 9.0e5),
            Position.fromDegrees(45, -120, 7.0e5),
            Position.fromDegrees(40, -125, 5.0e5)
        );
        Polygon poly = new Polygon(positions);
        layer.addRenderable(poly);

        // Create a terrain following polygon with the default attributes, and the default path type (GREAT_CIRCLE).
        positions = Arrays.asList(
            Position.fromDegrees(40, -105, 0),
            Position.fromDegrees(45, -110, 0),
            Position.fromDegrees(50, -100, 0),
            Position.fromDegrees(45, -90, 0),
            Position.fromDegrees(40, -95, 0)
        );
        poly = new Polygon(positions);
        poly.setAltitudeMode(WorldWind.CLAMP_TO_GROUND); // clamp the polygon vertices to the ground
        poly.setFollowTerrain(true); // follow the ground between polygon vertices
        layer.addRenderable(poly);

        // Create an extruded polygon with the default attributes, the default altitude mode (ABSOLUTE),
        // and the default path type (GREAT_CIRCLE).
        positions = Arrays.asList(
            Position.fromDegrees(20, -135, 5.0e5),
            Position.fromDegrees(25, -140, 7.0e5),
            Position.fromDegrees(30, -130, 9.0e5),
            Position.fromDegrees(25, -120, 7.0e5),
            Position.fromDegrees(20, -125, 5.0e5)
        );
        poly = new Polygon(positions);
        poly.setExtrude(true); // extrude the polygon from the ground to each polygon position's altitude
        layer.addRenderable(poly);

        // Create an extruded polygon with custom attributes that display the extruded vertical lines,
        // make the extruded interior 50% transparent, and increase the polygon line with.
        positions = Arrays.asList(
            Position.fromDegrees(20, -105, 5.0e5),
            Position.fromDegrees(25, -110, 7.0e5),
            Position.fromDegrees(30, -100, 9.0e5),
            Position.fromDegrees(25, -90, 7.0e5),
            Position.fromDegrees(20, -95, 5.0e5)
        );
        ShapeAttributes attrs = new ShapeAttributes();
        attrs.setDrawVerticals(true); // display the extruded verticals
        attrs.setInteriorColor(new Color(1, 1, 1, 0.5f)); // 50% transparent white
        attrs.setOutlineWidth(3);
        poly = new Polygon(positions, attrs);
        poly.setExtrude(true); // extrude the polygon from the ground to each polygon position's altitude
        layer.addRenderable(poly);

        // Create a polygon with an inner hole by specifying multiple polygon boundaries
        poly = new Polygon();
        poly.addBoundary(Arrays.asList(
            Position.fromDegrees(0, -135, 5.0e5),
            Position.fromDegrees(5, -140, 7.0e5),
            Position.fromDegrees(10, -130, 9.0e5),
            Position.fromDegrees(5, -120, 7.0e5),
            Position.fromDegrees(0, -125, 5.0e5)
        ));
        poly.addBoundary(Arrays.asList(
            Position.fromDegrees(2.5, -130, 6.0e5),
            Position.fromDegrees(5.0, -135, 7.0e5),
            Position.fromDegrees(7.5, -130, 8.0e5),
            Position.fromDegrees(5.0, -125, 7.0e5)
        ));
        layer.addRenderable(poly);

        // Create an extruded polygon with an inner hole and custom attributes that display the extruded vertical lines,
        // make the extruded interior 50% transparent, and increase the polygon line with.
        poly = new Polygon(attrs);
        poly.addBoundary(Arrays.asList(
            Position.fromDegrees(0, -105, 5.0e5),
            Position.fromDegrees(5, -110, 7.0e5),
            Position.fromDegrees(10, -100, 9.0e5),
            Position.fromDegrees(5, -90, 7.0e5),
            Position.fromDegrees(0, -95, 5.0e5)
        ));
        poly.addBoundary(Arrays.asList(
            Position.fromDegrees(2.5, -100, 6.0e5),
            Position.fromDegrees(5.0, -105, 7.0e5),
            Position.fromDegrees(7.5, -100, 8.0e5),
            Position.fromDegrees(5.0, -95, 7.0e5)
        ));
        poly.setExtrude(true); // extrude the polygon from the ground to each polygon position's altitude
        layer.addRenderable(poly);

        return wwd;
    }
}
