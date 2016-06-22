/*
 * Copyright (c) 2016 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwindx;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import java.util.Arrays;

import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.layer.RenderableLayer;
import gov.nasa.worldwind.layer.ShowTessellationLayer;
import gov.nasa.worldwind.render.ImageSource;
import gov.nasa.worldwind.shape.SurfaceImage;

public class TextureStressTestActivity extends BasicGlobeActivity {

    protected RenderableLayer layer = new RenderableLayer();

    protected Sector firstSector = new Sector();

    protected Sector sector = new Sector();

    protected Bitmap bitmap;

    protected Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            if (msg.what == ADD_IMAGE) {
                return addImage();
            } else {
                return false;
            }
        }
    });

    protected static final int ADD_IMAGE = 0;

    protected static final int ADD_IMAGE_DELAY = 1000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setAboutBoxTitle("About the " + this.getResources().getText(R.string.title_texture_stress_test));
        this.setAboutBoxText("Continuously allocates OpenGL texture objects to test the effect of an excessive number of textures on the World Wind render resource cache.");

        // Setup the World Window to display the tessellation layer and a layer of surface images. We use a minimal
        // layer configuration in order to gather precise metrics on memory usage.
        this.getWorldWindow().getLayers().clearLayers();
        this.getWorldWindow().getLayers().addLayer(new ShowTessellationLayer());
        this.getWorldWindow().getLayers().addLayer(this.layer);

        // Position the viewer so that the surface images will be visible as they're added.
        this.firstSector.set(35.0, 10.0, 0.5, 0.5);
        this.sector.set(this.firstSector);
        this.getWorldWindow().getNavigator().setLatitude(37.5);
        this.getWorldWindow().getNavigator().setLongitude(15.0);
        this.getWorldWindow().getNavigator().setAltitude(1.0e6);

        // Allocate a 32-bit 1024 x 1024 bitmap that we'll use to create all of the OpenGL texture objects in this test.
        int[] colors = new int[1024 * 1024];
        Arrays.fill(colors, 0xFF00FF00);
        this.bitmap = Bitmap.createBitmap(colors, 1024, 1024, Bitmap.Config.ARGB_8888);
    }

    protected boolean addImage() {
        // Create an image source with a unique factory instance. This pattern is used in order to force World Wind to
        // allocate a new OpenGL texture object for each surface image from a single bitmap instance.
        ImageSource imageSource = ImageSource.fromBitmapFactory(new ImageSource.BitmapFactory() {
            @Override
            public Bitmap createBitmap() {
                return bitmap;
            }
        });

        // Add the surface image to this test's layer.
        this.layer.addRenderable(new SurfaceImage(new Sector(this.sector), imageSource));
        this.getWorldWindow().requestRedraw();

        // Advance to the next surface image's location.
        if (this.sector.maxLongitude() < this.firstSector.minLongitude() + this.firstSector.deltaLongitude() * 20) {
            this.sector.set(
                this.sector.minLatitude(), this.sector.minLongitude() + this.sector.deltaLongitude() + 0.1,
                this.sector.deltaLatitude(), this.sector.deltaLongitude());
        } else {
            this.sector.set(
                this.sector.minLatitude() + this.sector.deltaLatitude() + 0.1, this.firstSector.minLongitude(),
                this.sector.deltaLatitude(), this.sector.deltaLongitude());
        }

        // Add another image after the configured delay.
        return this.handler.sendEmptyMessageDelayed(ADD_IMAGE, ADD_IMAGE_DELAY);
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Stop adding images when this Activity is paused.
        this.handler.removeMessages(ADD_IMAGE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Add images to the World Window at a regular interval.
        this.handler.sendEmptyMessageDelayed(ADD_IMAGE, ADD_IMAGE_DELAY);
    }
}
