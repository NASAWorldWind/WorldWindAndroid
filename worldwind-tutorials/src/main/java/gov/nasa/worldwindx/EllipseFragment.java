/*
 * Copyright (c) 2018 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwindx;

import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.WorldWindow;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.layer.RenderableLayer;
import gov.nasa.worldwind.render.Color;
import gov.nasa.worldwind.shape.Ellipse;
import gov.nasa.worldwind.shape.ShapeAttributes;

public class EllipseFragment extends BasicGlobeFragment {

    /**
     * Creates a new WorldWindow (GLSurfaceView) object with a set of Ellipse shapes
     *
     * @return The WorldWindow object containing the globe.
     */
    @Override
    public WorldWindow createWorldWindow() {
        // Let the super class (BasicGlobeFragment) do the creation
        WorldWindow wwd = super.createWorldWindow();

        // Create a layer in which to display the ellipse shapes. In this tutorial, we use a new instance of
        // RenderableLayer. Like all Renderable objects, Ellipse shapes may be organized into any arrangement of layers.
        RenderableLayer tutorialLayer = new RenderableLayer();
        wwd.getLayers().addLayer(tutorialLayer);

        // Create a surface ellipse with the default attributes, a 500km major-radius and a 300km minor-radius. Surface
        // ellipses are configured with a CLAMP_TO_GROUND altitudeMode and followTerrain set to true.
        Ellipse ellipse = new Ellipse(new Position(45, -120, 0), 500000, 300000);
        ellipse.setAltitudeMode(WorldWind.CLAMP_TO_GROUND); // clamp the ellipse's center position to the terrain surface
        ellipse.setFollowTerrain(true); // cause the ellipse geometry to follow the terrain surface
        tutorialLayer.addRenderable(ellipse);

        // Create a surface ellipse with with custom attributes that make the interior 50% transparent and increase the
        // outline width.
        ShapeAttributes attrs = new ShapeAttributes();
        attrs.setInteriorColor(new Color(1, 1, 1, 0.5f)); // 50% transparent white
        attrs.setOutlineWidth(3);
        ellipse = new Ellipse(new Position(45, -100, 0), 500000, 300000, attrs);
        ellipse.setAltitudeMode(WorldWind.CLAMP_TO_GROUND); // clamp the ellipse's center position to the terrain surface
        ellipse.setFollowTerrain(true); // cause the ellipse geometry to follow the terrain surface
        tutorialLayer.addRenderable(ellipse);

        // Create a surface ellipse with a heading of 45 degrees, causing the semi-major axis to point Northeast and the
        // semi-minor axis to point Southeast.
        ellipse = new Ellipse(new Position(35, -120, 0), 500000, 300000);
        ellipse.setAltitudeMode(WorldWind.CLAMP_TO_GROUND); // clamp the ellipse's center position to the terrain surface
        ellipse.setFollowTerrain(true); // cause the ellipse geometry to follow the terrain surface
        ellipse.setHeading(45);
        tutorialLayer.addRenderable(ellipse);

        // Create a surface circle with the default attributes and 400km radius.
        ellipse = new Ellipse(new Position(35, -100, 0), 400000, 400000);
        ellipse.setAltitudeMode(WorldWind.CLAMP_TO_GROUND); // clamp the ellipse's center position to the terrain surface
        ellipse.setFollowTerrain(true); // cause the ellipse geometry to follow the terrain surface
        tutorialLayer.addRenderable(ellipse);

        return wwd;
    }
}
