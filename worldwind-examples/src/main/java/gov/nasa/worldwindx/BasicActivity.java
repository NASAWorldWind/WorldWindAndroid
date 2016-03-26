package gov.nasa.worldwindx;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import gov.nasa.worldwind.WorldWindow;
import gov.nasa.worldwind.layer.BlueMarbleBackgroundLayer;
import gov.nasa.worldwind.layer.BlueMarbleLandsatLayer;
import gov.nasa.worldwind.layer.BlueMarbleLayer;
import gov.nasa.worldwindx.layer.AtmosphereLayer;

public class BasicActivity extends AppCompatActivity {

    protected WorldWindow wwd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Create the World Window and set it as the content view for this activity.
        this.wwd = new WorldWindow(this);
        this.setContentView(this.wwd);

        // Setup the World Window's layers.
        this.wwd.getLayers().addLayer(new BlueMarbleBackgroundLayer(this));
        this.wwd.getLayers().addLayer(new BlueMarbleLayer());
        this.wwd.getLayers().addLayer(new BlueMarbleLandsatLayer());
        this.wwd.getLayers().addLayer(new AtmosphereLayer());

        // Configure the Blue Marble & Landsat layer to appear after zooming in.
        this.wwd.getLayers().getLayer(2).setMaxActiveAltitude(1.0e6);
    }

    @Override
    protected void onPause() {
        super.onPause();
        this.wwd.onPause(); // pauses the rendering thread
    }

    @Override
    protected void onResume() {
        super.onResume();
        this.wwd.onResume(); // resumes a paused rendering thread
    }
}
