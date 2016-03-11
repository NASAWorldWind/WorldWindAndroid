/*
 * Copyright (c) 2016 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.util;

import android.content.Context;
import android.support.annotation.RawRes;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class WWUtil {

    public static String readRawResourceAsString(Context context, @RawRes int id) throws IOException {

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
