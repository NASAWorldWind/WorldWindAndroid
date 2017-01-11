/*
 * Copyright (c) 2017 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwindx;

import android.content.Context;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import gov.nasa.worldwind.util.Logger;
import gov.nasa.worldwind.util.WWUtil;

public class TutorialUtil {

    public static File unpackAsset(Context context, String fileName) {
        InputStream in = null;
        OutputStream out = null;
        try {
            File file = new File(context.getCacheDir(), fileName);
            if (file.exists()) {
                return file;
            }

            in = new BufferedInputStream(context.getAssets().open(fileName));
            out = new BufferedOutputStream(new FileOutputStream(file));

            byte[] buffer = new byte[4096];
            int count;

            while ((count = in.read(buffer)) != -1) {
                out.write(buffer, 0, count);
            }

            out.flush();

            return file;
        } catch (Exception ex) {
            Logger.logMessage(Logger.ERROR, "TutorialUtil", "unpackAsset", "Exception unpacking " + fileName, ex);
            return null;
        } finally {
            WWUtil.closeSilently(in);
            WWUtil.closeSilently(out);
        }
    }
}
