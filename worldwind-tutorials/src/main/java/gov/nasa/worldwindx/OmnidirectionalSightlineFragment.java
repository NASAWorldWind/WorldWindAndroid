/*
 * Copyright (c) 2017 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwindx;

import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.WorldWindow;
import gov.nasa.worldwind.geom.LookAt;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.layer.RenderableLayer;
import gov.nasa.worldwind.render.Color;
import gov.nasa.worldwind.render.ImageSource;
import gov.nasa.worldwind.shape.OmnidirectionalSightline;
import gov.nasa.worldwind.shape.Placemark;
import gov.nasa.worldwind.shape.ShapeAttributes;

public class OmnidirectionalSightlineFragment extends BasicGlobeFragment {

    /**
     * Creates a new WorldWindow (GLSurfaceView) object with an OmnidirectionalSightline
     *
     * @return The WorldWindow object containing the globe.
     */
    @Override
    public WorldWindow createWorldWindow() {
        // Let the super class (BasicGlobeFragment) do the creation
        WorldWindow wwd = super.createWorldWindow();

        // Specify the sightline position, which is the origin of the line of sight calculation
        Position position = new Position(46.230, -122.190, 2500.0);
        // Specify the range of the sightline (meters)
        double range = 10000.0;
        // Create attributes for the visible terrain
        ShapeAttributes visibleAttributes = new ShapeAttributes();
        visibleAttributes.setInteriorColor(new Color(0f, 1f, 0f, 0.5f));
        // Create attributes for the occluded terrain
        ShapeAttributes occludedAttributes = new ShapeAttributes();
        occludedAttributes.setInteriorColor(new Color(0.1f, 0.1f, 0.1f, 0.8f));

        // Create the sightline
        OmnidirectionalSightline sightline = new OmnidirectionalSightline(position, range);
        // Set the attributes
        sightline.setAttributes(visibleAttributes);
        sightline.setOccludeAttributes(occludedAttributes);

        // Create a layer for the sightline
        RenderableLayer sightlineLayer = new RenderableLayer();
        sightlineLayer.addRenderable(sightline);
        wwd.getLayers().addLayer(sightlineLayer);

        // Create a Placemark to visualize the position of the sightline
        this.createPlacemark(position, sightlineLayer);

        // Position the camera to look at the line of site terrain coverage
        this.positionView(wwd);

        return wwd;
    }

    protected void createPlacemark(Position position, RenderableLayer layer) {
        Placemark placemark = new Placemark(position);
        placemark.getAttributes().setImageSource(ImageSource.fromResource(R.drawable.aircraft_fixwing));
        placemark.getAttributes().setImageScale(2);
        placemark.getAttributes().setDrawLeader(true);
        layer.addRenderable(placemark);
    }

    protected void positionView(WorldWindow wwd) {
        LookAt lookAt = new LookAt().set(46.230, -122.190, 500, WorldWind.ABSOLUTE, 1.5e4 /*range*/, 45.0 /*heading*/, 70.0 /*tilt*/, 0 /*roll*/);
        wwd.getNavigator().setAsLookAt(this.getWorldWindow().getGlobe(), lookAt);
    }
}
