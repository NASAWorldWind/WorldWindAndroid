package gov.nasa.worldwindx;

import android.content.res.Configuration;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import java.util.ArrayList;

import gov.nasa.worldwind.WorldWindow;
import gov.nasa.worldwind.layer.BackgroundLayer;
import gov.nasa.worldwind.layer.BlueMarbleLandsatLayer;
import gov.nasa.worldwindx.experimental.AtmosphereLayer;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;

/**
 * This activity manifests two side-by-side globes with an adjustable splitter
 */
public class MultiGlobeActivity extends AbstractMainActivity {
    /**
     * This protected member allows derived classes to override the resource used in setContentView.
     */
    protected int layoutResourceId = R.layout.activity_globe;
    protected int deviceOrientation;
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
        this.deviceOrientation = getResources().getConfiguration().orientation;
        performLayout();
    }

    private void performLayout() {
        releaseWorldWindows();

        // Get the standard/common layout used for a single globe activity
        // and replace it's contents with a multi-globe layout.
        RelativeLayout layout = (RelativeLayout) findViewById(R.id.globe_content);
        layout.removeAllViews();

        // Add the landscape or portrait layout
        View multiGlobeLayout = getLayoutInflater().inflate(R.layout.multi_globe_content, null);
        layout.addView(multiGlobeLayout);

        // Add a WorldWindow to each of the FrameLayouts in the multi-globe layout.
        FrameLayout globe1 = (FrameLayout) findViewById(R.id.globe_one);
        FrameLayout globe2 = (FrameLayout) findViewById(R.id.globe_two);
        ImageButton splitter = (ImageButton) findViewById(R.id.splitter);

        globe1.addView(getWorldWindow(0) == null ? createWorldWindow() : getWorldWindow(0), new FrameLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT));
        globe2.addView(getWorldWindow(1) == null ? createWorldWindow() : getWorldWindow(1), new FrameLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT));
        splitter.setOnTouchListener(new SplitterTouchListener(globe1, globe2, splitter));
    }


    private void releaseWorldWindows() {
        for (WorldWindow wwd : worldWindows) {
            ((ViewGroup) wwd.getParent()).removeView(wwd);
        }
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
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        // Checks the orientation of the screen
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            //Toast.makeText(this, "landscape", Toast.LENGTH_SHORT).show();
        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
            //Toast.makeText(this, "portrait", Toast.LENGTH_SHORT).show();
        }
        deviceOrientation = newConfig.orientation;
        performLayout();
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
            return null;
        }
        return this.worldWindows.get(index);
    }

    private class SplitterTouchListener implements View.OnTouchListener {

        private final FrameLayout one;
        private final FrameLayout two;
        private final ImageButton splitter;
        private final int splitterWeight;

        public SplitterTouchListener(FrameLayout one, FrameLayout two, ImageButton splitter) {
            this.one = one;
            this.two = two;
            this.splitter = splitter;
            this.splitterWeight = 30;   // TODO: compute this value
        }

        /**
         * Called when a touch event is dispatched to a view. This allows listeners to
         * get a chance to respond before the target view.
         *
         * @param v     The view the touch event has been dispatched to.
         * @param event The MotionEvent object containing full information about
         *              the event.
         * @return True if the listener has consumed the event, false otherwise.
         */
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_MOVE:
                    // Get screen coordinates of the touch point
                    float rawX = event.getRawX();
                    float rawY = event.getRawY();
                    // Get the primary layout container for the multi-globe display
                    LinearLayout parent = (LinearLayout) findViewById(R.id.multi_globe_content);
                    // Get the layoutParams for each of the children. The parent will layout the
                    // children based on the layout weights computed based on the splitter position.
                    LinearLayout.LayoutParams layout1 = (LinearLayout.LayoutParams) one.getLayoutParams();
                    LinearLayout.LayoutParams layout2 = (LinearLayout.LayoutParams) two.getLayoutParams();
                    LinearLayout.LayoutParams layout3 = (LinearLayout.LayoutParams) splitter.getLayoutParams();

                    int weightSum;
                    if (deviceOrientation == Configuration.ORIENTATION_LANDSCAPE) {
                        // We're using the pixel values for the layout weights, with a fixed weight
                        // for the splitter.
                        weightSum = parent.getWidth();
                        layout1.weight = Math.min(Math.max(0f, rawX - (splitterWeight / 2f)), weightSum - splitterWeight);
                        layout2.weight = Math.min(Math.max(0f, weightSum - layout1.weight - splitterWeight), weightSum - splitterWeight);
                        parent.setWeightSum(weightSum);
                    } else {
                        // We're using the pixel values for the layout weights, with a fixed weight
                        // for the splitter.  In portrait mode we have a header that we must account for.
                        int origin[] = new int[2];
                        parent.getLocationOnScreen(origin);
                        float y = rawY - origin[1];
                        weightSum = parent.getHeight();
                        layout2.weight = Math.min(Math.max(0f, y - (splitterWeight / 2f)), weightSum - splitterWeight);
                        layout1.weight = Math.min(Math.max(0f, weightSum - layout2.weight - splitterWeight), weightSum - splitterWeight);
                        parent.setWeightSum(weightSum);
                    }
                    layout3.weight = splitterWeight;

                    one.setLayoutParams(layout1);
                    two.setLayoutParams(layout2);
                    splitter.setLayoutParams(layout3);

                    break;
            }
            return false;
        }
    }

}

