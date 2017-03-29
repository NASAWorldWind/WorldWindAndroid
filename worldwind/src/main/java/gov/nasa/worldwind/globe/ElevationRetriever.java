/*
 * Copyright (c) 2017 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.globe;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.ShortBuffer;

import gov.nasa.worldwind.render.ImageSource;
import gov.nasa.worldwind.render.ImageTile;
import gov.nasa.worldwind.util.Logger;
import gov.nasa.worldwind.util.Retriever;
import gov.nasa.worldwind.util.WWUtil;

public class ElevationRetriever extends Retriever<ImageTile, Void, ShortBuffer> {

    public ElevationRetriever(int maxSimultaneousRetrievals) {
        super(maxSimultaneousRetrievals);
    }

    @Override
    protected void retrieveAsync(ImageTile tile, Void unused, Callback<ImageTile, Void, ShortBuffer> callback) {
        try {
            ShortBuffer buffer = this.decodeCoverage(tile.getImageSource());

            if (buffer != null) {
                callback.retrievalSucceeded(this, tile, unused, buffer);
            } else {
                callback.retrievalFailed(this, tile, null); // failed but no exception
            }
        } catch (Throwable logged) {
            callback.retrievalFailed(this, tile, logged); // failed with exception
        }
    }

    protected ShortBuffer decodeCoverage(ImageSource imageSource) throws IOException {
        if (imageSource.isUrl()) {
            return this.decodeUrl(imageSource.asUrl());
        }

        return this.decodeUnrecognized(imageSource);
    }

    protected ShortBuffer decodeUrl(String urlString) throws IOException {
        // TODO establish a file caching service for remote resources
        // TODO retry absent resources, they are currently handled but suppressed entirely after the first failure
        // TODO configurable connect and read timeouts

        InputStream stream = null;
        try {
            URLConnection conn = new URL(urlString).openConnection();
            conn.setConnectTimeout(3000);
            conn.setReadTimeout(30000);

            byte[] page = new byte[1024 * 16];
            ByteBuffer buffer = ByteBuffer.allocate(page.length);
            int readCount;

            stream = new BufferedInputStream(conn.getInputStream());
            while ((readCount = stream.read(page, 0, page.length)) != -1) {
                if (readCount > buffer.remaining()) {
                    ByteBuffer newBuffer = ByteBuffer.allocate(buffer.capacity() + page.length);
                    newBuffer.put((ByteBuffer) buffer.flip());
                    buffer = newBuffer;
                }

                buffer.put(page, 0, readCount);
            }

            buffer.flip();

            return buffer.order(ByteOrder.LITTLE_ENDIAN).asShortBuffer();
        } finally {
            WWUtil.closeSilently(stream);
        }
    }

    protected ShortBuffer decodeUnrecognized(ImageSource imageSource) {
        Logger.log(Logger.WARN, "Unrecognized image source \'" + imageSource + "\'");
        return null;
    }
}
