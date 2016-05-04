/*
 * Copyright (c) 2016 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwindx;

import android.os.Bundle;
import android.util.SparseArray;

import armyc2.c2sd.renderer.utilities.ModifiersUnits;
import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.geom.LookAt;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.layer.LayerList;
import gov.nasa.worldwind.layer.RenderableLayer;
import gov.nasa.worldwind.shape.Placemark;
import gov.nasa.worldwindx.milstd2525.MilStd2525;

public class PlacemarksMilStd2525Activity extends BasicGlobeActivity {

    protected RenderableLayer symbolLayer = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setAboutBoxTitle("About the " + getResources().getText(R.string.title_placemarks_milstd2525));
        setAboutBoxText("Demonstrates how to add MilStd2525C Symbols to a RenderableLayer.");

        // Create a layer for the military symbols and add it just before the Atmosphere layer
        this.symbolLayer = new RenderableLayer("Symbols");
        LayerList layers = this.getWorldWindow().getLayers();
        int index = layers.indexOfLayerNamed("Atmosphere");
        layers.addLayer(index, this.symbolLayer);

        // The rendering service takes time initialize. We'll display the globe
        // while the service is initializing on a background thread.
        initializeSymbols();

        // Set the camera to look at the area where the symbols will be displayed.
        Position pos = new Position(32.4520, 63.44553, 0);
        LookAt lookAt = new LookAt().set(pos.latitude, pos.longitude, pos.altitude, WorldWind.ABSOLUTE,
            1e5 /*range*/, 0 /*heading*/, 45 /*tilt*/, 0 /*roll*/);
        this.getWorldWindow().getNavigator().setAsLookAt(this.getWorldWindow().getGlobe(), lookAt);

    }

    /**
     * Initializes the symbol renderer on a background thread and loads the symbols after the initialization is
     * complete.
     */
    private void initializeSymbols() {
        WorldWind.taskService().execute(new Runnable() {
            @Override
            public void run() {

                // The symbol renderer can take a long time to initialize.
                MilStd2525.initializeRenderer(getApplicationContext());

                // TODO: demonstrate an efficient/foolproof way of adding renderables to a running application.
                // We must add the new renderables on the GLThread.
                getWorldWindow().queueEvent(new Runnable() {
                    @Override
                    public void run() {

                        // "MIL-STD-2525 Friendly SOF Drone Aircraft"
                        SparseArray<String> modifiers = new SparseArray<String>();
                        modifiers.put(ModifiersUnits.Q_DIRECTION_OF_MOVEMENT, "235");
                        Placemark drone = new Placemark(
                            Position.fromDegrees(32.4520, 63.44553, 3000),
                            MilStd2525.getPlacemarkAttributes("SFAPMFQM--GIUSA", modifiers, null));

                        symbolLayer.addRenderable(drone);

                        // "MIL-STD-2525 Hostile Self-Propelled Rocket Launchers"
                        modifiers.clear();
                        modifiers.put(ModifiersUnits.Q_DIRECTION_OF_MOVEMENT, "90");
                        modifiers.put(ModifiersUnits.AJ_SPEED_LEADER, "0.1");
                        Placemark launcher = new Placemark(
                            Position.fromDegrees(32.4014, 63.3894, 0),
                            MilStd2525.getPlacemarkAttributes("SHGXUCFRMS----G", modifiers, null));

                        symbolLayer.addRenderable(launcher);

                        // "MIL-STD-2525 Friendly Heavy Machine Gun"
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

                        // Signal a change in the WorldWind scene; requestRender() is callable from any thread.
                        getWorldWindow().requestRender();
                    }
                });

            }
        });
    }

}
