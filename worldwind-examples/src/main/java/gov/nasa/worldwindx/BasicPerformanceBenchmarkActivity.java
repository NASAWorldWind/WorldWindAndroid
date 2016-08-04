/*
 * Copyright (c) 2016 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwindx;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.v4.util.Pools;
import android.view.MotionEvent;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.WorldWindow;
import gov.nasa.worldwind.WorldWindowController;
import gov.nasa.worldwind.geom.Camera;
import gov.nasa.worldwind.geom.Location;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.layer.Layer;
import gov.nasa.worldwind.layer.RenderableLayer;
import gov.nasa.worldwind.render.ImageSource;
import gov.nasa.worldwind.shape.Placemark;
import gov.nasa.worldwind.shape.PlacemarkAttributes;
import gov.nasa.worldwind.util.Logger;
import gov.nasa.worldwind.util.WWMath;
import gov.nasa.worldwind.util.WWUtil;

public class BasicPerformanceBenchmarkActivity extends GeneralGlobeActivity {

    public static class NoOpWorldWindowController implements WorldWindowController {

        @Override
        public WorldWindow getWorldWindow() {
            return null;
        }

        @Override
        public void setWorldWindow(WorldWindow wwd) {
        }

        @Override
        public boolean onTouchEvent(MotionEvent event) {
            return false;
        }
    }

    public static class AnimateCameraCommand implements Runnable {

        protected WorldWindow wwd;

        protected Camera beginCamera = new Camera();

        protected Camera endCamera = new Camera();

        protected Camera curCamera = new Camera();

        protected Position beginPos = new Position();

        protected Position endPos = new Position();

        protected Position curPos = new Position();

        protected int steps;

        public AnimateCameraCommand(WorldWindow wwd, Camera end, int steps) {
            this.wwd = wwd;
            this.endCamera.set(end);
            this.endPos.set(end.latitude, end.longitude, end.altitude);
            this.steps = steps;
        }

        @Override
        public void run() {
            this.wwd.getNavigator().getAsCamera(this.wwd.getGlobe(), this.beginCamera);
            this.beginPos.set(this.beginCamera.latitude, this.beginCamera.longitude, this.beginCamera.altitude);

            for (int i = 0; i < this.steps; i++) {

                double amount = (double) i / (double) (this.steps - 1);
                this.beginPos.interpolateAlongPath(this.endPos, WorldWind.GREAT_CIRCLE, amount, this.curPos);

                this.curCamera.latitude = this.curPos.latitude;
                this.curCamera.longitude = this.curPos.longitude;
                this.curCamera.altitude = this.curPos.altitude;
                this.curCamera.heading = WWMath.interpolateAngle360(amount, this.beginCamera.heading, this.endCamera.heading);
                this.curCamera.tilt = WWMath.interpolateAngle180(amount, this.beginCamera.tilt, this.endCamera.tilt);
                this.curCamera.roll = WWMath.interpolateAngle180(amount, this.beginCamera.roll, this.endCamera.roll);

                Runnable setCommand = SetCameraCommand.obtain(this.wwd, this.curCamera);
                runOnActivityThread(setCommand);
                sleepQuietly(FRAME_INTERVAL);
            }
        }
    }

    public static class SetCameraCommand implements Runnable {

        private static Pools.Pool<SetCameraCommand> pool = new Pools.SynchronizedPool<>(10);

        private WorldWindow wwd;

        private Camera camera = new Camera();

        private SetCameraCommand() {
        }

        public static SetCameraCommand obtain(WorldWindow wwd, Camera camera) {
            SetCameraCommand command = pool.acquire();
            if (command == null) {
                command = new SetCameraCommand();
            }

            return command.set(wwd, camera);
        }

        private SetCameraCommand set(WorldWindow wwd, Camera camera) {
            this.wwd = wwd;
            this.camera.set(camera);
            return this;
        }

        private SetCameraCommand reset() {
            this.wwd = null;
            return this;
        }

        @Override
        public void run() {
            this.wwd.getNavigator().setAsCamera(this.wwd.getGlobe(), this.camera);
            this.wwd.requestRedraw();
            pool.release(this.reset());
        }
    }

    public static class SleepCommand implements Runnable {

        protected long durationMillis;

        public SleepCommand(long durationMillis) {
            this.durationMillis = durationMillis;
        }

        @Override
        public void run() {
            sleepQuietly(this.durationMillis);
        }
    }

    public static class LogFrameMetricsCommand implements Runnable {

        protected WorldWindow wwd;

        public LogFrameMetricsCommand(WorldWindow wwd) {
            this.wwd = wwd;
        }

        @Override
        public void run() {
            if (!isActivityThread()) {
                runOnActivityThread(this);
            } else {
                Logger.log(Logger.INFO, this.wwd.getFrameMetrics().toString());
            }
        }
    }

    public static class ClearFrameMetricsCommand implements Runnable {

        protected WorldWindow wwd;

        public ClearFrameMetricsCommand(WorldWindow wwd) {
            this.wwd = wwd;
        }

        @Override
        public void run() {
            if (!isActivityThread()) {
                runOnActivityThread(this);
            } else {
                this.wwd.getFrameMetrics().reset();
            }
        }
    }

    protected static final int FRAME_INTERVAL = 67; // 67 millis; 15 frames per second

    protected static Handler activityHandler = new Handler(Looper.getMainLooper());

    protected static ExecutorService commandExecutor;

    public static ExecutorService getNewCommandExecutor() {
        commandExecutor = Executors.newSingleThreadExecutor();
        return commandExecutor;
    }

    public static void runOnActivityThread(@NonNull Runnable command) {
        activityHandler.post(command);
    }

    public static boolean isActivityThread() {
        return Thread.currentThread() == Looper.getMainLooper().getThread();
    }

    public static void sleepQuietly(long durationMillis) {
        try {
            Thread.sleep(durationMillis);
        } catch (InterruptedException ignored) {
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setAboutBoxTitle("About the " + this.getResources().getText(R.string.title_basic_performance_benchmark));
        this.setAboutBoxText("Executes a basic performance benchmark, logging the accumulated frame statistics.");

        // Suppress the World Window's built-in navigation behavior.
        WorldWindow wwd = this.getWorldWindow();
        wwd.setWorldWindowController(new NoOpWorldWindowController());

        // Add a layer containing a large number of placemarks.
        this.getWorldWindow().getLayers().addLayer(this.createPlacemarksLayer());
    }

    @Override
    protected void onStart() {
        super.onStart();

        // Create location objects for the places used in this test.
        Location arc = new Location(37.415229, -122.06265);
        Location gsfc = new Location(38.996944, -76.848333);
        Location esrin = new Location(41.826947, 12.674122);

        // After a 1 second initial delay, clear the frame statistics associated with this test.
        Executor exec = getNewCommandExecutor();   // gets a new instance
        exec.execute(new SleepCommand(1000));
        exec.execute(new ClearFrameMetricsCommand(wwd));

        // After a 1/2 second delay, fly to NASA Ames Research Center over 100 frames.
        Camera cam = new Camera(arc.latitude, arc.longitude, 10e3, WorldWind.ABSOLUTE, 0, 0, 0);
        exec.execute(new AnimateCameraCommand(wwd, cam, 100));

        // After a 1/2 second delay, rotate the camera to look at NASA Goddard Space Flight Center over 50 frames.
        double azimuth = arc.greatCircleAzimuth(gsfc);
        cam = new Camera(arc.latitude, arc.longitude, 10e3, WorldWind.ABSOLUTE, azimuth, 70, 0);
        exec.execute(new SleepCommand(500));
        exec.execute(new AnimateCameraCommand(wwd, cam, 50));

        // After a 1/2 second delay, fly the camera to NASA Goddard Space Flight Center over 200 frames.
        Location midLoc = arc.interpolateAlongPath(gsfc, WorldWind.GREAT_CIRCLE, 0.5, new Location());
        azimuth = midLoc.greatCircleAzimuth(gsfc);
        exec.execute(new SleepCommand(500));
        cam = new Camera(midLoc.latitude, midLoc.longitude, 1000e3, WorldWind.ABSOLUTE, azimuth, 0, 0);
        exec.execute(new AnimateCameraCommand(wwd, cam, 100));
        cam = new Camera(gsfc.latitude, gsfc.longitude, 10e3, WorldWind.ABSOLUTE, azimuth, 70, 0);
        exec.execute(new AnimateCameraCommand(wwd, cam, 100));

        // After a 1/2 second delay, rotate the camera to look at ESA Centre for Earth Observation over 50 frames.
        azimuth = gsfc.greatCircleAzimuth(esrin);
        cam = new Camera(gsfc.latitude, gsfc.longitude, 10e3, WorldWind.ABSOLUTE, azimuth, 90, 0);
        exec.execute(new SleepCommand(500));
        exec.execute(new AnimateCameraCommand(wwd, cam, 50));

        // After a 1/2 second delay, fly the camera to ESA Centre for Earth Observation over 200 frames.
        midLoc = gsfc.interpolateAlongPath(esrin, WorldWind.GREAT_CIRCLE, 0.5, new Location());
        exec.execute(new SleepCommand(500));
        cam = new Camera(midLoc.latitude, midLoc.longitude, 1000e3, WorldWind.ABSOLUTE, azimuth, 60, 0);
        exec.execute(new AnimateCameraCommand(wwd, cam, 100));
        cam = new Camera(esrin.latitude, esrin.longitude, 100e3, WorldWind.ABSOLUTE, azimuth, 30, 0);
        exec.execute(new AnimateCameraCommand(wwd, cam, 100));

        // After a 1/2 second delay, back the camera out to look at ESA Centre for Earth Observation over 100 frames.
        cam = new Camera(esrin.latitude, esrin.longitude, 2000e3, WorldWind.ABSOLUTE, 0, 0, 0);
        exec.execute(new SleepCommand(500));
        exec.execute(new AnimateCameraCommand(wwd, cam, 100));

        // After a 1 second delay, log the frame statistics associated with this test.
        exec.execute(new SleepCommand(1000));
        exec.execute(new LogFrameMetricsCommand(wwd));
    }

    @Override
    protected void onStop() {
        super.onStop();
        commandExecutor.shutdownNow();
    }

    protected Layer createPlacemarksLayer() {

        RenderableLayer layer = new RenderableLayer("Placemarks");
        PlacemarkAttributes[] attrs = {
            PlacemarkAttributes.createWithImage(ImageSource.fromResource(R.drawable.aircraft_fixwing)),
            PlacemarkAttributes.createWithImage(ImageSource.fromResource(R.drawable.airplane)),
            PlacemarkAttributes.createWithImage(ImageSource.fromResource(R.drawable.airport)),
            PlacemarkAttributes.createWithImage(ImageSource.fromResource(R.drawable.airport_terminal))};

        BufferedReader reader = null;
        try {
            InputStream in = this.getResources().openRawResource(R.raw.world_apts);
            reader = new BufferedReader(new InputStreamReader(in));

            // The first line is the CSV header:
            //  LAT,LON,ALT,NAM,IKO,NA3,USE,USEdesc
            String line = reader.readLine();
            List<String> headers = Arrays.asList(line.split(","));
            final int LAT = headers.indexOf("LAT");
            final int LON = headers.indexOf("LON");
            final int NA3 = headers.indexOf("NA3");
            final int USE = headers.indexOf("USE");

            // Read the remaining lines
            int attrIndex = 0;
            while ((line = reader.readLine()) != null) {
                String[] fields = line.split(",");
                if (fields[NA3].startsWith("US") && fields[USE].equals("49")) { // display USA Civilian/Public airports
                    Position pos = Position.fromDegrees(Double.parseDouble(fields[LAT]), Double.parseDouble(fields[LON]), 0);
                    layer.addRenderable(new Placemark(pos, attrs[attrIndex++ % attrs.length]));
                }
            }

        } catch (IOException e) {
            Logger.log(Logger.ERROR, "Exception attempting to read Airports database");
        } finally {
            WWUtil.closeSilently(reader);
        }

        return layer;
    }

}
