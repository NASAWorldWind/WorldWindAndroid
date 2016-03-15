/*
 * Copyright (c) 2016 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.DrawableRes;
import android.support.annotation.RawRes;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class WWUtil {

    public static Bitmap readResourceAsBitmap(Context context, @DrawableRes int id) {
        if (context == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "WWUtil", "readResourceAsBitmap", "missingContext"));

        }

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inScaled = false; // load the bitmap in its native dimensions

        return BitmapFactory.decodeResource(context.getResources(), id, options);
    }

    public static String readResourceAsText(Context context, @RawRes int id) throws IOException {
        if (context == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "WWUtil", "readResourceAsText", "missingContext"));

        }

        BufferedReader reader = null;
        try {
            InputStream in = context.getResources().openRawResource(id);
            reader = new BufferedReader(new InputStreamReader(in));

            StringBuilder sb = new StringBuilder();
            String line;

            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }

            return sb.toString();
        } finally {
            if (reader != null) {
                reader.close();
            }
        }
    }
}
