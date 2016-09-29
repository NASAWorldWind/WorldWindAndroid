/*
 * Copyright (c) 2016 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwindx;

import android.graphics.Typeface;

import org.w3c.dom.Text;

import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.WorldWindow;
import gov.nasa.worldwind.geom.LookAt;
import gov.nasa.worldwind.geom.Offset;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.layer.RenderableLayer;
import gov.nasa.worldwind.render.Color;
import gov.nasa.worldwind.shape.Label;
import gov.nasa.worldwind.shape.Placemark;
import gov.nasa.worldwind.shape.TextAttributes;

public class LabelsFragment extends BasicGlobeFragment {

    /**
     * Creates a new WorldWindow with a RenderableLayer populated with four Labels.
     */
    @Override
    public WorldWindow createWorldWindow() {
        // Let the super class (BasicGlobeFragment) do the creation
        WorldWindow wwd = super.createWorldWindow();

        // Create a RenderableLayer for labels and add it to the WorldWindow
        RenderableLayer layer = new RenderableLayer("Renderables");
        wwd.getLayers().addLayer(layer);

        // Create a simple label with the default text attributes
        Label ventura = new Label(new Position(34.281, -119.293, 0), "Ventura");
        layer.addRenderable(ventura);

        // Create a big yellow label from a text attributes bundle
        TextAttributes textAttributes = new TextAttributes()
            .setTypeface(Typeface.create("serif", Typeface.BOLD_ITALIC))
            .setTextColor(new Color(1f, 1f, 0f, 1f))// yellow, opaque
            .setTextSize(50f);                      // default size is 24
        Label island = new Label(new Position(34.005, -119.392, 0),
            "Anacapa Island", textAttributes);
        layer.addRenderable(island);

        // Create a collection of labels that demonstrate
        // label offsets from a given position.
        final Position pos = new Position(34.2, -119.5, 0);
        Offset northEast = new Offset(
            WorldWind.OFFSET_PIXELS, -40,       // move left-edge right
            WorldWind.OFFSET_PIXELS, -40);      // move lower-edge up
        Offset northWest = new Offset(
            WorldWind.OFFSET_INSET_PIXELS, -40, // move right-edge left
            WorldWind.OFFSET_PIXELS, -40);      // move lower-edge up
        Offset southWest = new Offset(
            WorldWind.OFFSET_INSET_PIXELS, -40, // move right-edge left
            WorldWind.OFFSET_INSET_PIXELS, -40);// move top-edge down
        Offset southEast = new Offset(
            WorldWind.OFFSET_PIXELS, -40,       // move left-edge right
            WorldWind.OFFSET_INSET_PIXELS, -40);// move top-edge down

        Label label1 = new Label(pos, "NW: " + northWest + " _", new TextAttributes().setTextOffset(northWest));
        Label label2 = new Label(pos, "SW: " + southWest + " ¯", new TextAttributes().setTextOffset(southWest));
        Label label3 = new Label(pos, "_ NE: " + northEast, new TextAttributes().setTextOffset(northEast));
        Label label4 = new Label(pos, "¯ SE: " + southEast, new TextAttributes().setTextOffset(southEast));
        Label label5 = new Label(pos, "default");   // anchor point is bottomCenter of label

        layer.addRenderable(label1);
        layer.addRenderable(label2);
        layer.addRenderable(label3);
        layer.addRenderable(label4);
        layer.addRenderable(label5);
        layer.addRenderable(
            Placemark.createWithColorAndSize(pos,
                new Color(android.graphics.Color.YELLOW), 10));


        // And finally, for this demo, position the viewer to look at the airport placemark
        // from a tilted perspective when this Android activity is created.
        LookAt lookAt = new LookAt().set(pos.latitude, pos.longitude, pos.altitude, WorldWind.ABSOLUTE,
            1e5 /*range*/, 0 /*heading*/, 0 /*tilt*/, 0 /*roll*/);
        wwd.getNavigator().setAsLookAt(wwd.getGlobe(), lookAt);

        return wwd;
    }
}
