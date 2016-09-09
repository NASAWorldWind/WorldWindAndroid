package gov.nasa.worldwindx;

import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

import java.util.ArrayList;

import gov.nasa.worldwind.WorldWindow;
import gov.nasa.worldwind.layer.BackgroundLayer;
import gov.nasa.worldwind.layer.BlueMarbleLandsatLayer;
import gov.nasa.worldwind.util.Logger;
import gov.nasa.worldwindx.experimental.AtmosphereLayer;

/**
 * This activity manifests two side-by-side globes.
 */
public class MultiGlobeActivity extends AbstractMainActivity {

    /**
     * This protected member allows derived classes to override the resource used in setContentView.
     */
    protected int layoutResourceId = R.layout.activity_globe;

    /**
     * The WorldWindow (GLSurfaceView) maintained by this activity
     */
    protected ArrayList<WorldWindow> worldWindows = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Establish the activity content
        setContentView(this.layoutResourceId);
        super.onCreate(savedInstanceState);
        setAboutBoxTitle("About the " + this.getResources().getText(R.string.title_multi_globe));
        setAboutBoxText("Demonstrates multiple globes.");

        // Get the standard/common layout used for a single globe activity
        // and replace it's contents with a multi-globe layout.
        RelativeLayout layout = (RelativeLayout) findViewById(R.id.globe_content);
        View multiGlobeLayout = getLayoutInflater().inflate(R.layout.multi_globe_content, null);
        layout.removeAllViewsInLayout();
        layout.addView(multiGlobeLayout);

        // Add a WorldWindow to each of the FrameLayouts in the multi-globe layout.
        FrameLayout globe1 = (FrameLayout) findViewById(R.id.globe_one);
        FrameLayout globe2 = (FrameLayout) findViewById(R.id.globe_two);
        globe1.addView(createWorldWindow());
        globe2.addView(createWorldWindow());
    }

    private WorldWindow createWorldWindow() {
        // Create the World Window (a GLSurfaceView) which displays the globe.
        WorldWindow wwd = new WorldWindow(this);
        // Setup the World Window's layers.
        wwd.getLayers().addLayer(new BackgroundLayer());
        wwd.getLayers().addLayer(new BlueMarbleLandsatLayer());
        wwd.getLayers().addLayer(new AtmosphereLayer());

        this.worldWindows.add(wwd);

        return wwd;
    }

    @Override
    protected void onPause() {
        super.onPause();
        for (WorldWindow wwd : this.worldWindows) {
            wwd.onPause();// pauses the rendering thread
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        for (WorldWindow wwd : this.worldWindows) {
            wwd.onResume(); // resumes a paused rendering thread
        }
    }

    @Override
    public WorldWindow getWorldWindow() {
        return this.getWorldWindow(0);
    }

    public WorldWindow getWorldWindow(int index) {
        if (index >= this.worldWindows.size()) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "MultiGlobeActivity", "getWorldWindow", "index out of range."));
        }
        return this.worldWindows.get(index);
    }
}

