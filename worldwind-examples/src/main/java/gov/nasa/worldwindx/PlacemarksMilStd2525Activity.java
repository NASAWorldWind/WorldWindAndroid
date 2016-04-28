/*
 * Copyright (c) 2016 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwindx;

import android.os.Bundle;
import android.os.Handler;
import android.util.SparseArray;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import armyc2.c2sd.renderer.utilities.ModifiersUnits;
import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.geom.LookAt;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.layer.LayerList;
import gov.nasa.worldwind.layer.RenderableLayer;
import gov.nasa.worldwindx.milstd2525.MilStd2525Renderer;
import gov.nasa.worldwindx.milstd2525.MilStd2525Placemark;

public class PlacemarksMilStd2525Activity extends BasicGlobeActivity {

    protected static Executor executorService = Executors.newSingleThreadExecutor();

    protected Handler activityHandler = new Handler();

    protected RenderableLayer symbolLayer = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setAboutBoxTitle("About the " + getResources().getText(R.string.title_placemarks));
        setAboutBoxText("Demonstrates how to add Placemarks to a RenderableLayer.");

        // Create a layer for the military symbols and add it just before the Atmosphere layer
        this.symbolLayer = new RenderableLayer("Symbols");
        LayerList layers = this.getWorldWindow().getLayers();
        int index = layers.indexOfLayerNamed("Atmosphere");
        layers.addLayer(index, this.symbolLayer);

        // The rendering service takes time initialize. We'll display the globe while the service is initializing
        // on a background thread.
        initializeSymbols();


        // Set the camera to look at the area where the symbols will be displayed.
        Position pos = new Position(32.4520, 63.44553, 0);
        LookAt lookAt = new LookAt().set(pos.latitude, pos.longitude, pos.altitude, WorldWind.ABSOLUTE,
            1e5 /*range*/, 0 /*heading*/, 45 /*tilt*/, 0 /*roll*/);
        this.getWorldWindow().getNavigator().setAsLookAt(this.getWorldWindow().getGlobe(), lookAt);
    }

    private void initializeSymbols() {
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                // The symbol renderer can take a long time to initialize.
                MilStd2525Renderer.initialize(getApplicationContext());

                getWorldWindow().queueEvent(new Runnable() {
                    @Override
                    public void run() {

                        // "MIL-STD-2525 Friendly SOF Drone Aircraft"
                        SparseArray<String> modifiers = new SparseArray<String>();
                        modifiers.put(ModifiersUnits.Q_DIRECTION_OF_MOVEMENT, "235");
                        MilStd2525Placemark droneSymbol = MilStd2525Placemark.fromSymbolCode(
                            Position.fromDegrees(32.4520, 63.44553, 3000), "SFAPMFQM--GIUSA", modifiers);
                        // TODO: try queueEvent()
                        // TODO: lookat WorldWindow executor service
                        // TODO: lookat AxyncTask
                        symbolLayer.addRenderable(droneSymbol);

                        // "MIL-STD-2525 Hostile Self-Propelled Rocket Launchers"
                        modifiers.clear();
                        modifiers.put(ModifiersUnits.Q_DIRECTION_OF_MOVEMENT, "90");
                        modifiers.put(ModifiersUnits.AJ_SPEED_LEADER, "0.1");
                        symbolLayer.addRenderable(MilStd2525Placemark.fromSymbolCode(
                            Position.fromDegrees(32.4014, 63.3894, 0), "SHGXUCFRMS----G", modifiers));

                        // "MIL-STD-2525 Friendly Heavy Machine Gun"
                        modifiers.clear();
                        modifiers.put(ModifiersUnits.C_QUANTITY, "200");
                        modifiers.put(ModifiersUnits.G_STAFF_COMMENTS, "FOR REINFORCEMENTS");
                        modifiers.put(ModifiersUnits.H_ADDITIONAL_INFO_1, "ADDED SUPPORT FOR JJ");
                        modifiers.put(ModifiersUnits.V_EQUIP_TYPE, "MACHINE GUN");
                        modifiers.put(ModifiersUnits.W_DTG_1, "30140000ZSEP97");    // Date/Time Group
                        symbolLayer.addRenderable(MilStd2525Placemark.fromSymbolCode(
                            Position.fromDegrees(32.3902, 63.4161, 0), "SFGPEWRH--MTUSG", modifiers));

                        // Signal a change in the WorldWind scene; requestRender() is callable from any thread.
                        // TODO: requestRender() isn't working consistently when called from a background thread. Why?
                        getWorldWindow().requestRender();
                    }
                });

            }
        });
    }

}
