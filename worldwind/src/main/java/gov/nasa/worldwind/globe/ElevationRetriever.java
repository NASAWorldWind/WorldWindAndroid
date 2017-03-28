/*
 * Copyright (c) 2017 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.globe;

import java.io.IOException;
import java.nio.ShortBuffer;

import gov.nasa.worldwind.util.Retriever;

public class ElevationRetriever extends Retriever<String, Void, ShortBuffer> {

    public ElevationRetriever(int maxSimultaneousRetrievals) {
        super(maxSimultaneousRetrievals);
    }

    @Override
    protected void retrieveAsync(String coverageSource, Void unused, Callback<String, Void, ShortBuffer> callback) {
        try {
            ShortBuffer buffer = this.decodeCoverage(coverageSource);

            if (buffer != null) {
                callback.retrievalSucceeded(this, coverageSource, unused, buffer);
            } else {
                callback.retrievalFailed(this, coverageSource, null); // failed but no exception
            }
        } catch (Throwable logged) {
            callback.retrievalFailed(this, coverageSource, logged); // failed with exception
        }
    }

    protected ShortBuffer decodeCoverage(String coverageSource) throws IOException {
        // TODO establish a file caching service for remote resources
        // TODO retry absent resources, they are currently handled but suppressed entirely after the first failure
        // TODO configurable connect and read timeouts

//        InputStream stream = null;
//        try {
//            URLConnection conn = new URL(coverageSource).openConnection();
//            conn.setConnectTimeout(3000);
//            conn.setReadTimeout(30000);
//
//            stream = new BufferedInputStream(conn.getInputStream());
//        } finally {
//            WWUtil.closeSilently(stream);
//        }

        return null;
    }
}
