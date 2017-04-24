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

import gov.nasa.worldwind.formats.tiff.Subfile;
import gov.nasa.worldwind.formats.tiff.Tiff;
import gov.nasa.worldwind.render.ImageSource;
import gov.nasa.worldwind.util.Logger;
import gov.nasa.worldwind.util.Retriever;
import gov.nasa.worldwind.util.SynchronizedPool;
import gov.nasa.worldwind.util.WWUtil;

public class ElevationRetriever extends Retriever<ImageSource, Void, ShortBuffer> {

    protected SynchronizedPool<byte[]> pagePool = new SynchronizedPool<>();

    protected SynchronizedPool<ByteBuffer> bufferPool = new SynchronizedPool<>();

    protected static final int PAGE_SIZE = 1024 * 16;

    protected static final int BUFFER_SIZE = 1024 * 132;

    public ElevationRetriever(int maxSimultaneousRetrievals) {
        super(maxSimultaneousRetrievals);
    }

    @Override
    protected void retrieveAsync(ImageSource key, Void unused, Callback<ImageSource, Void, ShortBuffer> callback) {
        try {
            ShortBuffer buffer = this.decodeCoverage(key);

            if (buffer != null) {
                callback.retrievalSucceeded(this, key, unused, buffer);
            } else {
                callback.retrievalFailed(this, key, null); // failed but no exception
            }
        } catch (Throwable logged) {
            callback.retrievalFailed(this, key, logged); // failed with exception
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

            stream = new BufferedInputStream(conn.getInputStream());
            String contentType = conn.getContentType();
            if (contentType.equalsIgnoreCase("application/bil16")) {
                return this.readInt16Data(stream);
            } else if (contentType.equalsIgnoreCase("image/tiff")) {
                return this.readTiffData(stream);
            } else {
                throw new RuntimeException(
                    Logger.logMessage(Logger.ERROR, "ElevationRetriever", "decodeUrl", "Format not supported"));
            }
        } finally {
            WWUtil.closeSilently(stream);
        }
    }

    protected ShortBuffer decodeUnrecognized(ImageSource imageSource) {
        Logger.log(Logger.WARN, "Unrecognized image source \'" + imageSource + "\'");
        return null;
    }

    protected ShortBuffer readTiffData(InputStream stream) throws IOException {

        ByteBuffer tiffBuffer = this.bufferPool.acquire();
        if (tiffBuffer == null) {
            tiffBuffer = ByteBuffer.allocate(BUFFER_SIZE);
        }
        tiffBuffer.clear();
        ByteBuffer buffer = this.bufferStream(stream, tiffBuffer);
        Tiff tiff = new Tiff(buffer);
        Subfile subfile = tiff.getSubfiles().get(0);
        // check that the format of the subfile matches our supported data types
        if (this.isTiffFormatSupported(subfile)) {
            int dataSize = subfile.getDataSize();
            ByteBuffer result = subfile.getData(ByteBuffer.allocate(dataSize));
            result.clear();

            this.bufferPool.release(tiffBuffer);
            return result.asShortBuffer();
        } else {
            throw new RuntimeException(
                Logger.logMessage(Logger.ERROR, "ElevationRetriever", "readTiffData", "Tiff file format not supported"));
        }
    }

    protected boolean isTiffFormatSupported(Subfile subfile) {
        return subfile.getSampleFormat()[0] == Tiff.TWOS_COMP_SIGNED_INT &&
            subfile.getBitsPerSample()[0] == 16 &&
            subfile.getSamplesPerPixel() == 1 &&
            subfile.getCompression() == 1;
    }

    protected ShortBuffer readInt16Data(InputStream stream) throws IOException {
        ShortBuffer result = this.bufferStream(stream, ByteBuffer.allocate(BUFFER_SIZE)).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer();
        return result;
    }

    protected ByteBuffer bufferStream(InputStream stream, ByteBuffer buffer) throws IOException {
        byte[] page = this.pagePool.acquire();
        if (page == null) {
            page = new byte[PAGE_SIZE];
        }

        int readCount;
        while ((readCount = stream.read(page, 0, page.length)) != -1) {
            if (readCount > buffer.remaining()) {
                ByteBuffer newBuffer = ByteBuffer.allocate(buffer.capacity() + page.length);
                newBuffer.put((ByteBuffer) buffer.flip());
                buffer = newBuffer;
            }

            buffer.put(page, 0, readCount);
        }

        buffer.flip();
        this.pagePool.release(page);

        return buffer;
    }
}
