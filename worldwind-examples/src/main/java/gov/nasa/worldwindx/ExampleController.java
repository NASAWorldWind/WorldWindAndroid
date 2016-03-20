/*
 * Copyright (c) 2016 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwindx;


import gov.nasa.worldwind.BasicNavigatorController;
import gov.nasa.worldwind.WorldWindow;
import gov.nasa.worldwind.geom.Matrix4;
import gov.nasa.worldwind.geom.Vec3;
import gov.nasa.worldwind.render.DrawContext;

public class ExampleController extends BasicNavigatorController {

    protected Vec3 lightDirection = new Vec3(0, 0, 1);

    protected Matrix4 lightTransform = new Matrix4().setToRotation(0, 1, 0, -0.1);

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
        this.getWorldWindow().requestRender();
    }

    @Override
    public void windowWillDraw(DrawContext dc) {

        // Rotate the light direction after the composition sequence completes.
        this.lightDirection.multiplyByMatrix(this.lightTransform);

        // Attach the light direction to the draw context.
        dc.putUserProperty("lightDirection", this.lightDirection);
    }

    @Override
    public void windowDidDraw(DrawContext dc) {

        // Rotate the viewer after the composition sequence completes.
        WorldWindow wwd = this.getWorldWindow();
        wwd.getNavigator().getPosition().longitude -= 0.03;
        dc.requestRender();
    }
}
