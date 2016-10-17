/*
 * Copyright (c) 2016 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.util;

import android.opengl.GLSurfaceView;
import android.util.Log;

import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.egl.EGLDisplay;

public class WWConfigChooser implements GLSurfaceView.EGLConfigChooser {

    static private final String kTag = "GDC11";

    private int[] mValue;

    int numConfigs = 64;

    @Override
    public EGLConfig chooseConfig(EGL10 egl, EGLDisplay display) {
        mValue = new int[1];

        int[] fastConfig = {
            EGL10.EGL_RED_SIZE, 5,
            EGL10.EGL_GREEN_SIZE, 6,
            EGL10.EGL_BLUE_SIZE, 5,
            EGL10.EGL_ALPHA_SIZE, 0,
            EGL10.EGL_DEPTH_SIZE, 16,
            EGL10.EGL_NONE
        };

        int[] bestConfig = {
            EGL10.EGL_RED_SIZE, 8,
            EGL10.EGL_GREEN_SIZE, 8,
            EGL10.EGL_BLUE_SIZE, 8,
            EGL10.EGL_ALPHA_SIZE, 8,
            EGL10.EGL_DEPTH_SIZE, 16,
            EGL10.EGL_NONE
        };

        // Get all matching configurations.
        // See: https://www.khronos.org/registry/egl/sdk/docs/man/html/eglChooseConfig.xhtml
        EGLConfig[] configs = new EGLConfig[numConfigs];
        if (!egl.eglChooseConfig(display, bestConfig, configs, numConfigs, mValue)) {
            throw new IllegalArgumentException("data eglChooseConfig failed");
        }
        numConfigs = mValue[0];
        Log.d(WWConfigChooser.class.getSimpleName(), "eglChooseConfig returned " + numConfigs + "configurations");


        // CAUTION! eglChooseConfigs returns configs with higher bit depth
        // first: Even though we asked for rgb565 configurations, rgb888
        // configurations are considered to be "better" and returned first.
        // You need to explicitly filter the data returned by eglChooseConfig!
        int index = -1;
        for (int i = 0; i < numConfigs; ++i) {
            Log.e(kTag, "config " + i);

            int r = findConfigAttrib(egl, display, configs[i], EGL10.EGL_RED_SIZE, 0);
            Log.e(kTag, "R " + r);

            int g = findConfigAttrib(egl, display, configs[i], EGL10.EGL_GREEN_SIZE, 0);
            Log.e(kTag, "G " + g);

            int b = findConfigAttrib(egl, display, configs[i], EGL10.EGL_BLUE_SIZE, 0);
            Log.e(kTag, "B " + b);

            int a = findConfigAttrib(egl, display, configs[i], EGL10.EGL_ALPHA_SIZE, 0);
            Log.e(kTag, "A " + a);

            int d = findConfigAttrib(egl, display, configs[i], EGL10.EGL_DEPTH_SIZE, 0);
            Log.e(kTag, "D " + d);

            if ((r == bestConfig[0]) && (g == bestConfig[1])
                && (b == bestConfig[2]) && (a == bestConfig[3])) {
                index = i;
                break;
            }
        }
        if (index == -1) {
            Log.w(kTag, "Did not find same config, using first");
            index = 0;
        }

        EGLConfig config = numConfigs > 0 ? configs[index] : null;
        if (config == null) {
            throw new IllegalArgumentException("No config chosen");
        }
        return config;
    }

    private int findConfigAttrib(EGL10 egl, EGLDisplay display,
                                 EGLConfig config, int attribute, int defaultValue) {
        if (egl.eglGetConfigAttrib(display, config, attribute, mValue)) {
            return mValue[0];
        }
        return defaultValue;
    }
}



