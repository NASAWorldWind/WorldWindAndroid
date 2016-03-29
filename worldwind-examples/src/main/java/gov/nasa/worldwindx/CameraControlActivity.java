/*
 * Copyright (c) 2016 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwindx;

import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.view.ViewStub;
import android.widget.NumberPicker;
import android.widget.TimePicker;

import gov.nasa.worldwind.geom.Camera;
import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.ogc.WmsLayer;
import gov.nasa.worldwind.ogc.WmsLayerConfig;

public class CameraControlActivity extends BasicGlobeActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        View v = this.findViewById(R.id.stub_camera_controls);
        if (v instanceof ViewStub)
            v = ((ViewStub) v).inflate();

        NumberPicker heading = (NumberPicker) findViewById(R.id.headingPicker);
        heading.setMaxValue(360);
        heading.setMinValue(0);
        heading.setWrapSelectorWheel(true);

        heading.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {

            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {

//                Camera camera = wwd.getNavigator().getAsCamera(wwd.getGlobe(), new Camera());
//                camera.heading = newVal;
//                wwd.getNavigator().setAsCamera(wwd.getGlobe(), camera);
                wwd.getNavigator().setHeading(newVal);
                wwd.requestRender();
            }
        });
        NumberPicker tilt = (NumberPicker) findViewById(R.id.tiltPicker);
        tilt.setMaxValue(360);
        tilt.setMinValue(0);
        tilt.setWrapSelectorWheel(true);

        tilt.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {

            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                wwd.getNavigator().setTilt(newVal);
                wwd.requestRender();
            }
        });
        NumberPicker roll = (NumberPicker) findViewById(R.id.rollPicker);
        roll.setMaxValue(360);
        roll.setMinValue(0);
        roll.setWrapSelectorWheel(true);

        roll.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {

            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                wwd.getNavigator().setRoll(newVal);
                wwd.requestRender();
            }
        });
    }
}
