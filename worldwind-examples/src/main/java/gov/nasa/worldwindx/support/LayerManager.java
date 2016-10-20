/*
 * Copyright (c) 2016 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwindx.support;

import android.app.Activity;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.view.Menu;
import android.view.MenuItem;

import gov.nasa.worldwind.WorldWindow;
import gov.nasa.worldwind.layer.Layer;
import gov.nasa.worldwind.layer.LayerList;
import gov.nasa.worldwindx.R;

/**
 * A rudimentary layer manager used by the examples.  Implemented as right-side drawer menu(NavigationView).
 */
public class LayerManager
    implements NavigationView.OnNavigationItemSelectedListener {

    @DrawableRes
    public final int LAYER_ENABLED_ICON = R.drawable.ic_menu_enabled;

    @DrawableRes
    public final int LAYER_DISABLED_ICON = R.drawable.ic_menu_disabled;

    protected DrawerLayout drawerLayout;

    protected NavigationView layerManagerMenu;

    protected WorldWindow wwd;

    /**
     * Constructs a LayerManager.
     *
     * @param context The Activity containing the layer manager layout resources.
     * @param wwd     The WorldWindow containing the LayerList to be managed.
     */
    public LayerManager(Activity context, WorldWindow wwd) {
        this.wwd = wwd;
        this.drawerLayout = (DrawerLayout) context.findViewById(R.id.drawer_layout);
        this.layerManagerMenu = (NavigationView) context.findViewById(R.id.layer_manager_drawer);
        this.layerManagerMenu.setNavigationItemSelectedListener(this);

        // Initialize the menu from the current WorldWindow's LayerList
        refresh();
    }

    /**
     * Adds a layer to the end of the layer list.
     *
     * @param layer The layer to be added.
     */
    public void addLayer(Layer layer) {
        this.wwd.getLayers().addLayer(layer);
        refresh();
    }

    /**
     * Inserts a layer in front of the named layer;
     *
     * @param name  The named layer where given layer should be added.
     * @param layer The layer to be added.
     */
    public void addLayerBeforeNamed(String name, Layer layer) {
        LayerList layers = this.wwd.getLayers();
        int index = layers.indexOfLayerNamed(name);
        if (index >= 0) {
            layers.addLayer(index, layer);
        } else {
            layers.addLayer(layer);
        }
        refresh();
    }

    /**
     * Adds a collection of layers to the end of the layer list.
     *
     * @param layers The layers to be added
     */
    public void addAllLayers(LayerList layers) {
        this.wwd.getLayers().addAllLayers(layers);
        refresh();
    }

    /**
     * Refreshes the menu content.
     */
    public void refresh() {
        Menu menu = this.layerManagerMenu.getMenu();
        menu.clear();
        final int groupId = 1;
        LayerList layers = this.wwd.getLayers();
        for (int i = 0; i < layers.count(); i++) {
            Layer layer = layers.getLayer(i);
            String layerName = layer.getDisplayName();
            menu.add(groupId, i /*item id*/, i /*order*/,
                layerName == null || layerName.isEmpty() ? layer.getClass().getSimpleName() : layerName)
                .setIcon(layer.isEnabled() ? LAYER_ENABLED_ICON : LAYER_DISABLED_ICON);
        }
    }

    /**
     * Toggles the enabled state of the layer menu items.
     *
     * @param item The menu item representing a layer
     *
     * @return True
     */
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Toggle the layer
        Layer layer = this.wwd.getLayers().getLayer(item.getItemId());
        layer.setEnabled(!layer.isEnabled());
        this.wwd.requestRedraw();

        // Update the selected menu item to reflected the layer's enabled state
        item.setIcon(layer.isEnabled() ? LAYER_ENABLED_ICON : LAYER_DISABLED_ICON);

        // Close the layer manager menu
        this.drawerLayout.closeDrawer(GravityCompat.END);
        return true;
    }
}
