/*
 * Copyright (c) 2016 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwindx;

import android.os.AsyncTask;
import android.os.Bundle;
import android.util.SparseArray;
import android.widget.FrameLayout;
import android.widget.TextView;

import armyc2.c2sd.renderer.utilities.ModifiersUnits;
import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.geom.LookAt;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.layer.RenderableLayer;
import gov.nasa.worldwind.shape.Placemark;
import gov.nasa.worldwindx.milstd2525.MilStd2525;

public class PlacemarksMilStd2525Activity extends GeneralGlobeActivity {

    protected TextView statusText = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setAboutBoxTitle("About the " + this.getResources().getText(R.string.title_placemarks_milstd2525));
        setAboutBoxText("Demonstrates how to add MilStd2525C Symbols to a RenderableLayer.");

        // Create a TextView to show the MIL-STD-2525 renderer's initialization status
        this.statusText = new TextView(this);
        this.statusText.setTextColor(android.graphics.Color.YELLOW);
        FrameLayout globeLayout = (FrameLayout) findViewById(R.id.globe);
        globeLayout.addView(this.statusText);

        // Set the camera to look at the area where the symbols will be displayed.
        Position pos = new Position(32.4520, 63.44553, 0);
        LookAt lookAt = new LookAt().set(pos.latitude, pos.longitude, pos.altitude, WorldWind.ABSOLUTE,
            1e5 /*range*/, 0 /*heading*/, 45 /*tilt*/, 0 /*roll*/);
        this.getWorldWindow().getNavigator().setAsLookAt(this.getWorldWindow().getGlobe(), lookAt);

        // The MIL-STD-2525 rendering library takes time initialize, we'll perform this task via the
        // AsyncTask's background thread and then load the symbols in its post execute handler.
        new InitializeSymbolsTask().execute();
    }

    /**
     * InitializeSymbolsTask is an AsyncTask that initializes the MIL-STD-2525 symbol renderer on a background thread
     * and then loads the symbols after the initialization is complete. This task must be instantiated and executed on
     * the UI Thread.
     */
    protected class InitializeSymbolsTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            statusText.setText("Initializing the MIL-STD-2525 Library and symbols...");
        }

        /**
         * Initialize the MIL-STD-2525 Rendering Library on a background thread.
         */
        @Override
        protected Void doInBackground(Void... notUsed) {
            // Time consuming operation . . .
            MilStd2525.initializeRenderer(getApplicationContext());
            return null;
        }

        /**
         * Update the symbol layer on the UI Thread.
         */
        @Override
        protected void onPostExecute(Void notUsed) {
            super.onPostExecute(notUsed);

            // Create a layer for the military symbols and add it to the WorldWindow
            RenderableLayer symbolLayer = new RenderableLayer("Symbols");
            getWorldWindow().getLayers().addLayer(symbolLayer);

            // Add a "MIL-STD-2525 Friendly SOF Drone Aircraft"
            SparseArray<String> modifiers = new SparseArray<String>();
            modifiers.put(ModifiersUnits.Q_DIRECTION_OF_MOVEMENT, "235");
            Placemark drone = new Placemark(
                Position.fromDegrees(32.4520, 63.44553, 3000),
                MilStd2525.getPlacemarkAttributes("SFAPMFQM--GIUSA", modifiers, null));

            symbolLayer.addRenderable(drone);

            // Add a "MIL-STD-2525 Hostile Self-Propelled Rocket Launchers"
            modifiers.clear();
            modifiers.put(ModifiersUnits.Q_DIRECTION_OF_MOVEMENT, "90");
            modifiers.put(ModifiersUnits.AJ_SPEED_LEADER, "0.1");
            Placemark launcher = new Placemark(
                Position.fromDegrees(32.4014, 63.3894, 0),
                MilStd2525.getPlacemarkAttributes("SHGXUCFRMS----G", modifiers, null));

            symbolLayer.addRenderable(launcher);

            // Add a "MIL-STD-2525 Friendly Heavy Machine Gun"
            modifiers.clear();
            modifiers.put(ModifiersUnits.C_QUANTITY, "200");
            modifiers.put(ModifiersUnits.G_STAFF_COMMENTS, "FOR REINFORCEMENTS");
            modifiers.put(ModifiersUnits.H_ADDITIONAL_INFO_1, "ADDED SUPPORT FOR JJ");
            modifiers.put(ModifiersUnits.V_EQUIP_TYPE, "MACHINE GUN");
            modifiers.put(ModifiersUnits.W_DTG_1, "30140000ZSEP97");    // Date/Time Group
            Placemark machineGun = new Placemark(
                Position.fromDegrees(32.3902, 63.4161, 0),
                MilStd2525.getPlacemarkAttributes("SFGPEWRH--MTUSG", modifiers, null));

            symbolLayer.addRenderable(machineGun);

            // Signal a change in the WorldWind scene; requestRedraw() is callable from any thread.
            //noinspection ResourceType
            getWorldWindow().requestRedraw();

            // Clear the status message set in onPreExecute
            statusText.setText("");
        }

    }

}
