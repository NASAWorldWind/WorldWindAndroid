/*
 * Copyright (c) 2016 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.util;

import android.util.Log;

import java.util.HashMap;
import java.util.Map;

public class Logger {

    public static final int ERROR = Log.ERROR;

    public static final int WARN = Log.WARN;

    public static final int INFO = Log.INFO;

    protected static String TAG = "gov.nasa.worldwind";

    protected static Map<String, String> messageTable;

    static {
        messageTable = new HashMap<>();
        messageTable.put("invalidBitmap", "The bitmap is null or recycled");
        messageTable.put("invalidClass", "The class is null or cannot be found");
        messageTable.put("invalidClipDistance", "The clip distance is invalid");
        messageTable.put("invalidFieldOfView", "The field of view is invalid");
        messageTable.put("invalidHeight", "The height is invalid");
        messageTable.put("invalidIndex", "The index is invalid");
        messageTable.put("invalidRadius", "The radius is invalid");
        messageTable.put("invalidResource", "The resource is invalid");
        messageTable.put("invalidSize", "The size is invalid");
        messageTable.put("invalidStride", "The stride is invalid");
        messageTable.put("invalidWidth", "The width is invalid");
        messageTable.put("missingArray", "The array is null or insufficient length");
        messageTable.put("missingBuffer", "The buffer is null or insufficient length");
        messageTable.put("missingGlobe", "The globe is null");
        messageTable.put("missingKey", "The key is null");
        messageTable.put("missingLayer", "The layer is null");
        messageTable.put("missingLevel", "The level is null");
        messageTable.put("missingList", "The list is null or empty");
        messageTable.put("missingLocation", "The location is null");
        messageTable.put("missingMatrix", "The matrix is null");
        messageTable.put("missingPosition", "The position is null");
        messageTable.put("missingProjection", "The projection is null");
        messageTable.put("missingRenderable", "The renderable is null");
        messageTable.put("missingResult", "The result argument is null");
        messageTable.put("missingSector", "The sector is null");
        messageTable.put("missingSource", "The source is null");
        messageTable.put("missingTile", "The tile is null");
        messageTable.put("missingTessellator", "The tessellator is null");
        messageTable.put("missingValue", "The value is null");
        messageTable.put("missingVector", "The vector is null");
        messageTable.put("singularMatrix", "The matrix cannot be inverted");
    }

    public static void log(int priority, String message) {
        if (Log.isLoggable(TAG, priority)) {
            Log.println(priority, TAG, message);
        }
    }

    public static void log(int priority, String message, Throwable tr) {
        if (Log.isLoggable(TAG, priority)) {
            Log.println(priority, TAG, message + '\n' + Log.getStackTraceString(tr));
        }
    }

    public static String logMessage(int level, String className, String methodName, String message) {
        String msg = makeMessage(className, methodName, message);
        log(level, msg);

        return msg;
    }

    public static String logMessage(int level, String className, String methodName, String message, Throwable tr) {
        String msg = makeMessage(className, methodName, message);
        log(level, msg, tr);

        return msg;
    }

    public static String makeMessage(String className, String methodName, String message) {
        StringBuilder sb = new StringBuilder();
        String msg = messageTable.get(message);
        sb.append(className).append(".").append(methodName);
        sb.append(": ").append(msg != null ? msg : message);

        return sb.toString();
    }
}
