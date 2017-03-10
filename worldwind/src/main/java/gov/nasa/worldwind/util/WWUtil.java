/*
 * Copyright (c) 2016 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.util;

import android.content.res.Resources;
import android.support.annotation.RawRes;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;

public class WWUtil {

    protected static final char LINE_SEPARATOR = '\n';

    /**
     * Closes a specified Closeable, suppressing any checked exceptions. This has no effect if the closeable is null.
     *
     * @param closeable the object to close, may be null in which case this does nothing
     */
    public static void closeSilently(Closeable closeable) {
        if (closeable == null) {
            return; // silently ignore null
        }

        try {
            closeable.close();
        } catch (RuntimeException rethrown) {
            throw rethrown;
        } catch (Exception ignored) {
            // silently ignore checked exceptions
        }
    }

    /**
     * Determines whether or not the specified string represents a URL. This returns false if the string is null.
     *
     * @param string the string in question
     *
     * @return true if the string represents a URL, otherwise false
     */
    public static boolean isUrlString(String string) {
        if (string == null) {
            return false;
        }

        try {
            new URL(string);
            return true; // no exception; the string is probably a valid URL
        } catch (MalformedURLException ignored) {
            return false; // silently ignore the exception
        }
    }

    public static String readResourceAsText(Resources resources, @RawRes int id) throws IOException {
        if (resources == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "WWUtil", "readResourceAsText", "missingResources"));

        }

        BufferedReader reader = null;
        try {
            InputStream in = resources.openRawResource(id);
            reader = new BufferedReader(new InputStreamReader(in));

            StringBuilder sb = new StringBuilder();
            String line;

            while ((line = reader.readLine()) != null) {
                sb.append(line).append(LINE_SEPARATOR);
            }

            return sb.toString();
        } finally {
            closeSilently(reader);
        }
    }
}
