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
        messageTable.put("missingLocation", "The specified location is null");
        messageTable.put("missingPosition", "The specified position is null");
        messageTable.put("missingResult", "The specified result argument is null");
    }

    public static void log(int level, String message) {
        if (Log.isLoggable(TAG, level)) {
            Log.println(level, TAG, message);
        }
    }

    public static String logMessage(int level, String className, String methodName, String message) {
        String msg = makeMessage(className, methodName, message);
        log(level, msg);

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
