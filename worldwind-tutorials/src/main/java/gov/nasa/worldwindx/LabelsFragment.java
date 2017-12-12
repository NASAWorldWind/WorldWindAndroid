/*
 * Copyright (c) 2016 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwindx;

import android.graphics.Typeface;

import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.WorldWindow;
import gov.nasa.worldwind.geom.Offset;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.layer.RenderableLayer;
import gov.nasa.worldwind.render.Color;
import gov.nasa.worldwind.shape.Label;
import gov.nasa.worldwind.shape.TextAttributes;

public class LabelsFragment extends BasicGlobeFragment {

    /**
     * Creates a new WorldWindow (GLSurfaceView) object with a set of label shapes
     *
     * @return The WorldWindow object containing the globe.
     */
    @Override
    public WorldWindow createWorldWindow() {
        // Let the super class (BasicGlobeFragment) do the creation
        WorldWindow wwd = super.createWorldWindow();

        // Create a layer to display the tutorial labels.
        RenderableLayer layer = new RenderableLayer();
        wwd.getLayers().addLayer(layer);

        // Create a basic label with the default attributes, including the default text color (white), the default text
        // size (24 pixels), the system default typeface, and the default alignment (bottom center).
        Label label = new Label(new Position(38.8977, -77.0365, 0), "The White House");
        layer.addRenderable(label);

        // Create a label with a green text color, the default text size, the system default typeface, and the default
        // alignment.
        TextAttributes attrs = new TextAttributes();
        attrs.setTextColor(new Color(0, 1, 0, 1)); // green via r,g,b,a
        label = new Label(new Position(38.881389, -77.036944, 0), "Thomas Jefferson Memorial", attrs);
        layer.addRenderable(label);

        // Create a right-aligned label using a bottom-right offset.
        attrs = new TextAttributes();
        attrs.setTextOffset(Offset.bottomRight());
        label = new Label(new Position(38.8893, -77.050111, 0), "Lincoln Memorial", attrs);
        layer.addRenderable(label);

        // Create a left-aligned label using a bottom-left offset.
        attrs = new TextAttributes();
        attrs.setTextOffset(Offset.bottomLeft());
        label = new Label(new Position(38.889803, -77.009114, 0), "United States Capitol", attrs);
        layer.addRenderable(label);

        // Create a label with a 48 pixel text size and a bold typeface.
        attrs = new TextAttributes();
        attrs.setTypeface(Typeface.DEFAULT_BOLD); // system default bold typeface
        attrs.setTextSize(48); // 48 screen pixels
        label = new Label(new Position(38.907192, -77.036871, 0), "Washington", attrs);
        layer.addRenderable(label);

        // Create a label with its orientation fixed relative to the globe.
        label = new Label(new Position(38.89, -77.023611, 0), "National Mall");
        label.setRotationMode(WorldWind.RELATIVE_TO_GLOBE);
        layer.addRenderable(label);

        // Place the viewer directly over the tutorial labels.
        wwd.getNavigator().setLatitude(38.89);
        wwd.getNavigator().setLongitude(-77.023611);
        wwd.getNavigator().setAltitude(10e3);

        return wwd;
    }
}
