/*
 * Copyright (c) 2016 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwindx;

import android.graphics.Typeface;

import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.WorldWindow;
import gov.nasa.worldwind.geom.LookAt;
import gov.nasa.worldwind.geom.Offset;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.layer.RenderableLayer;
import gov.nasa.worldwind.render.Color;
import gov.nasa.worldwind.shape.Label;
import gov.nasa.worldwind.shape.TextAttributes;

public class LabelsFragment extends BasicGlobeFragment {

    /**
     * Creates a new WorldWindow with a RenderableLayer populated with several Labels.
     */
    @Override
    public WorldWindow createWorldWindow() {
        // Let the super class (BasicGlobeFragment) do the creation
        WorldWindow wwd = super.createWorldWindow();

        // Create a RenderableLayer for labels and add it to the WorldWindow
        RenderableLayer layer = new RenderableLayer("Renderables");
        wwd.getLayers().addLayer(layer);

        // Create a simple label with the default text attributes:
        //  text size is 24,
        //  text alignment is bottom-center,
        //  text color is white.
        Label sanNicolas = new Label(new Position(33.262, -119.538, 0),
            "San Nicolas");
        layer.addRenderable(sanNicolas);

        // Define the text attributes for the Nat'l Park Service
        // using a big, bold, italic serif font.
        TextAttributes parkAttributes = new TextAttributes()
            .setTypeface(Typeface.create("serif", Typeface.BOLD_ITALIC))
            .setTextColor(new Color(0f, 1f, 0f, 0.5f))  // green, 50% opacity
            .setTextSize(50f);

        // Create a two-line label for the Nat'l Park, the top
        // label's bottom-center is aligned to the position,
        // the bottom label's top-center is aligned to the position.
        Label park1 = new Label(new Position(33.9, -119.7, 0),
            "Channel Islands", parkAttributes);

        Label park2 = new Label(new Position(33.9, -119.7, 0),
            "National Park",
            new TextAttributes(parkAttributes)
                .setTextOffset(Offset.topCenter()));
        layer.addRenderable(park1);
        layer.addRenderable(park2);

        // Define the text attributes used for the Nat'l Park islands
        // with a yellow, bold san-serif font
        TextAttributes islandAttributes = new TextAttributes()
            .setTypeface(Typeface.create("san-serif", Typeface.BOLD))
            .setTextColor(new Color(1f, 1f, 0f, 1f));// yellow, opaque

        // Create a labels for Santa Cruz and San Miguel using
        // the default bottom-center text alignment, but make
        // the text size larger than the default size (24).
        Label santaCruz = new Label(new Position(34.04, -119.77, 0),
            "Santa Cruz", new TextAttributes(islandAttributes)
                .setTextSize(40f));
        layer.addRenderable(santaCruz);

        Label sanMiguel = new Label(new Position(34.06, -120.37, 0),
            "Santa Miguel", new TextAttributes(islandAttributes)
                .setTextSize(32f));
        layer.addRenderable(sanMiguel);

        // Create a label for Santa Rosa where the top-right
        // of the text is aligned with the island's position.
        Label santaRosa = new Label(new Position(33.95, -120.15, 0),
            "Santa Rosa", new TextAttributes(islandAttributes)
                .setTextSize(40f)
                .setTextOffset(Offset.topRight()));
        layer.addRenderable(santaRosa);

        // Create smaller labels for Anacapa and Santa Barbara where
        // the bottom-left is aligned with the island's position.
        Label anacapa = new Label(new Position(34.005, -119.392, 0),
            "Anacapa", new TextAttributes(islandAttributes)
                .setTextSize(28f)
                .setTextOffset(Offset.bottomLeft()));
        layer.addRenderable(anacapa);

        Label santaBarbara = new Label(new Position(33.475, -119.037, 0),
            "Santa Barbara", anacapa.getAttributes());
        layer.addRenderable(santaBarbara);

        // Position the viewer to look down at the Channel Islands.
        final Position pos = new Position(33.7, -119.6, 0);
        LookAt lookAt = new LookAt().set(pos.latitude, pos.longitude, pos.altitude, WorldWind.ABSOLUTE,
            30e4 /*range*/, 0 /*heading*/, 0 /*tilt*/, 0 /*roll*/);
        wwd.getNavigator().setAsLookAt(wwd.getGlobe(), lookAt);

        return wwd;
    }
}
