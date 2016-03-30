/*
 * Copyright (c) 2016 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwindx;

import android.os.Bundle;
import android.view.View;
import android.view.ViewStub;
import android.widget.NumberPicker;

import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.WorldWindow;
import gov.nasa.worldwind.WorldWindowController;
import gov.nasa.worldwind.geom.Camera;

public class CameraControlActivity extends BasicGlobeActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWorldWindow().setWorldWindowController(new CustomWorldWindowCameraController());

        Camera camera = new Camera();
        camera.set(34.2, -119.2, 5000, WorldWind.ABSOLUTE, 0, 70, 0);
        this.getWorldWindow().getNavigator().setAsCamera(this.getWorldWindow().getGlobe(), camera);
    }
}
