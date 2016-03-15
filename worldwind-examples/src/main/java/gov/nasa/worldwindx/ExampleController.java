/*
 * Copyright (c) 2016 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwindx;

import android.os.Handler;

import gov.nasa.worldwind.BasicNavigatorController;
import gov.nasa.worldwind.WorldWindow;
import gov.nasa.worldwind.geom.Matrix4;
import gov.nasa.worldwind.geom.Vec3;
import gov.nasa.worldwind.layer.LayerList;
import gov.nasa.worldwind.render.DrawContext;

public class ExampleController extends BasicNavigatorController implements Runnable {

    protected Vec3 lightDirection = new Vec3(0, 0, 1);

    protected Matrix4 lightTransform = new Matrix4().setToRotation(0, 1, 0, -0.1);

    protected Handler handler = new Handler();

    protected int sequence = 0;

    protected int sequenceMillis = 5000;

    public ExampleController() {
    }

    @Override
    public void setWorldWindow(WorldWindow wwd) {
        super.setWorldWindow(wwd);

        // Position the navigator looking at North America from slightly above the equator.
        wwd.getNavigator().getPosition().longitude = -100;
        wwd.getNavigator().getPosition().latitude = 10;
        this.lightDirection.multiplyByMatrix(new Matrix4().setToRotation(0, 1, 0, -100));

        // Start a sequence that illustrates the globe's composition.
        this.run();
    }

    @Override
    public void windowWillDraw(DrawContext dc) {

        // Rotate the light direction after the composition sequence completes.
        if (this.sequence == 5) {
            this.lightDirection.multiplyByMatrix(this.lightTransform);
        }

        // Attach the light direction to the draw context.
        dc.putUserProperty("lightDirection", this.lightDirection);
    }

    @Override
    public void windowDidDraw(DrawContext dc) {

        // Rotate the viewer after the composition sequence completes.
        if (this.sequence == 5) {
            WorldWindow wwd = this.getWorldWindow();
            wwd.getNavigator().getPosition().longitude -= 0.03;
            dc.requestRender();
        }
    }

    @Override
    public void run() {
        LayerList layers = this.getWorldWindow().getLayers();

        switch (this.sequence) {
            case 0: // enable only wireframe
                layers.getLayer(1).setEnabled(true);
                layers.getLayer(2).setEnabled(false);
                layers.getLayer(3).setEnabled(false);
                layers.getLayer(4).setEnabled(false);
                this.sequence++;
                this.handler.postDelayed(this, this.sequenceMillis);
                this.getWorldWindow().requestRender();
                break;
            case 1: // disable wireframe and enable the simple image layer
                layers.getLayer(1).setEnabled(false);
                layers.getLayer(3).setEnabled(true);
                this.sequence++;
                this.handler.postDelayed(this, this.sequenceMillis);
                this.getWorldWindow().requestRender();
                break;
            case 2: // enable the sky atmospheric effect
                layers.getLayer(2).setEnabled(true);
                this.sequence++;
                this.handler.postDelayed(this, this.sequenceMillis);
                this.getWorldWindow().requestRender();
                break;
            case 3: // enable the ground atmospheric effect
                layers.getLayer(4).setEnabled(true);
                this.sequence++;
                this.handler.postDelayed(this, this.sequenceMillis);
                this.getWorldWindow().requestRender();
                break;
            case 4:
                this.sequence++;
                this.getWorldWindow().requestRender();
                break;
        }
    }
}
