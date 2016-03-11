package gov.nasa.worldwindx;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import gov.nasa.worldwind.WorldWindow;

public class WorldWindExamples extends AppCompatActivity {

    protected WorldWindow wwd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Create the World Window and set it as the content view for this activity.
        this.wwd = new WorldWindow(this);
        this.setContentView(this.wwd);

        // Setup the World Window's layers.
        this.wwd.getLayers().addLayer(new SimpleGlobeLayer());

        // Setup to spin the globe automatically.
        this.wwd.getNavigator().getPosition().latitude = 30;
        this.wwd.setNavigatorController(new SimpleNavigatorController());
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
