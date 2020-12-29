/*
 * Copyright (c) 2016 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.util;

import android.content.res.Resources;
import android.support.annotation.RawRes;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;

import gov.nasa.worldwind.WorldWind;

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


    /**
     * Checks Local Cache
     * */
    public static File checkLocalCache(boolean isForceOnline, String url, String parentDir) {
        if(!isForceOnline) {
            // ExUrl: https://worldwind26.arc.nasa.gov/elev?SERVICE=WMS&VERSION=1.3.0&REQUEST=GetMap&LAYERS=GEBCO,aster_v2,USGS-NED&STYLES=&CRS=EPSG:4326&BBOX=0.0,-90.0,90.0,0.0&WIDTH=256&HEIGHT=256&FORMAT=application/bil16&TRANSPARENT=TRUE
            // Important part 4 values of BBOX
            String eq = resolveBBOX(url);
            File dir = new File(parentDir);
            if(!dir.exists())
                dir.mkdirs();
            if(dir.canRead() && dir.canWrite()) {
                for (File file : dir.listFiles()) {
                    if (file.isFile()) {
                        if(file.getName().equals(eq)) {
                            return file;
                        }
                    }
                }
            }
        }
        return null;
    }

    public static String getFormat(String url) {
        //https://worldwind26.arc.nasa.gov/elev?SERVICE=WMS&VERSION=1.3.0&REQUEST=GetMap&LAYERS=GEBCO,aster_v2,USGS-NED&STYLES=&CRS=EPSG:4326&BBOX=-90.0,-180.0,0.0,-90.0&WIDTH=256&HEIGHT=256&FORMAT=application/bil16&TRANSPARENT=TRUE
        //&FORMAT=application/bil16&
        //bil16
        String[] formatSplt = url.split("FORMAT="); // 2 Parts
        String formatVal = formatSplt[1].split("&")[0].split("/")[1];
        return formatVal.toLowerCase();
    }

    public static String resolveBBOX(String url) {
        String[] bboxSplit = url.split("BBOX="); // 2 Parts
        String bboxValues = bboxSplit[1].split("&")[0];
        return bboxValues.replaceAll(",","a").replace(".","b").replace("-","c");
    }
}
