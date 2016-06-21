/*
 * Copyright (c) 2016 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind;

import java.util.Locale;

import gov.nasa.worldwind.draw.DrawContext;
import gov.nasa.worldwind.render.RenderContext;
import gov.nasa.worldwind.util.LruMemoryCache;

public class FrameMetrics {

    private final Object drawLock = new Object();

    protected TimeMetrics renderMetrics = new TimeMetrics();

    protected TimeMetrics drawMetrics = new TimeMetrics();

    protected CacheMetrics renderResourceCacheMetrics = new CacheMetrics();

    public FrameMetrics() {
    }

    public long getRenderTime() {
        return this.renderMetrics.time;
    }

    public double getRenderTimeAverage() {
        return this.computeTimeAverage(this.renderMetrics);
    }

    public double getRenderTimeStdDev() {
        return this.computeTimeStdDev(this.renderMetrics);
    }

    public long getRenderTimeTotal() {
        return this.renderMetrics.timeSum;
    }

    public long getRenderCount() {
        return this.renderMetrics.count;
    }

    public long getDrawTime() {
        synchronized (this.drawLock) {
            return this.drawMetrics.time;
        }
    }

    public double getDrawTimeAverage() {
        synchronized (this.drawLock) {
            return this.computeTimeAverage(this.drawMetrics);
        }
    }

    public double getDrawTimeStdDev() {
        synchronized (this.drawLock) {
            return this.computeTimeStdDev(this.drawMetrics);
        }
    }

    public long getDrawTimeTotal() {
        synchronized (this.drawLock) {
            return this.drawMetrics.timeSum;
        }
    }

    public long getDrawCount() {
        synchronized (this.drawLock) {
            return this.drawMetrics.count;
        }
    }

    public int getRenderResourceCacheCapacity() {
        return this.renderResourceCacheMetrics.capacity;
    }

    public int getRenderResourceCacheUsedCapacity() {
        return this.renderResourceCacheMetrics.usedCapacity;
    }

    public int getRenderResourceCacheEntryCount() {
        return this.renderResourceCacheMetrics.entryCount;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("FrameMetrics");
        sb.append("{renderMetrics={");
        this.printTimeMetrics(this.renderMetrics, sb);
        sb.append("}, drawMetrics={");
        this.printTimeMetrics(this.drawMetrics, sb);
        sb.append("}, renderResourceCacheMetrics={");
        this.printCacheMetrics(this.renderResourceCacheMetrics, sb);
        sb.append("}");

        return sb.toString();
    }

    public void beginRendering(RenderContext rc) {
        long now = System.currentTimeMillis();

        this.markBegin(this.renderMetrics, now);
    }

    public void endRendering(RenderContext rc) {
        long now = System.currentTimeMillis();
        this.markEnd(this.renderMetrics, now);
        this.assembleCacheMetrics(this.renderResourceCacheMetrics, rc.renderResourceCache);
    }

    public void beginDrawing(DrawContext dc) {
        long now = System.currentTimeMillis();

        synchronized (this.drawLock) {
            this.markBegin(this.drawMetrics, now);
        }
    }

    public void endDrawing(DrawContext dc) {
        long now = System.currentTimeMillis();

        synchronized (this.drawLock) {
            this.markEnd(this.drawMetrics, now);
        }
    }

    public void reset() {
        this.resetTimeMetrics(this.renderMetrics);

        synchronized (this.drawLock) {
            this.resetTimeMetrics(this.drawMetrics);
        }
    }

    protected void markBegin(TimeMetrics metrics, long timeMillis) {
        metrics.begin = timeMillis;
    }

    protected void markEnd(TimeMetrics metrics, long timeMillis) {
        metrics.time = timeMillis - metrics.begin;
        metrics.timeSum += metrics.time;
        metrics.timeSumOfSquares += (metrics.time * metrics.time);
        metrics.count++;
    }

    protected void resetTimeMetrics(TimeMetrics metrics) {
        // reset the metrics collected across multiple frames
        metrics.timeSum = 0;
        metrics.timeSumOfSquares = 0;
        metrics.count = 0;
    }

    protected double computeTimeAverage(TimeMetrics metrics) {
        if (metrics.count > 0) {
            return metrics.timeSum / (double) metrics.count;
        } else {
            return 0;
        }
    }

    protected double computeTimeStdDev(TimeMetrics metrics) {
        if (metrics.count > 0) {
            double avg = (double) metrics.timeSum / (double) metrics.count;
            double var = ((double) metrics.timeSumOfSquares / (double) metrics.count) - (avg * avg);
            return Math.sqrt(var);
        } else {
            return 0;
        }
    }

    protected void assembleCacheMetrics(CacheMetrics metrics, LruMemoryCache cache) {
        metrics.capacity = cache.getCapacity();
        metrics.usedCapacity = cache.getUsedCapacity();
        metrics.entryCount = cache.getEntryCount();
    }

    protected void printCacheMetrics(CacheMetrics metrics, StringBuilder out) {
        out.append("capacity=").append(String.format(Locale.US, "%,.0f", metrics.capacity / 1024.0)).append("KB");
        out.append(", usedCapacity=").append(String.format(Locale.US, "%,.0f", metrics.usedCapacity / 1024.0)).append("KB");
        out.append(", entryCount=").append(metrics.entryCount);
    }

    protected void printTimeMetrics(TimeMetrics metrics, StringBuilder out) {
        out.append("lastTime=").append(metrics.time).append("ms");
        out.append(", totalTime=").append(metrics.timeSum).append("ms");
        out.append(", count=").append(metrics.count);
        out.append(", avg=").append(String.format(Locale.US, "%.1f", this.computeTimeAverage(metrics))).append("ms");
        out.append(", stdDev=").append(String.format(Locale.US, "%.1f", this.computeTimeStdDev(metrics))).append("ms");
    }

    protected static class CacheMetrics {

        public int capacity;

        public int usedCapacity;

        public int entryCount;
    }

    protected static class TimeMetrics {

        public long begin;

        public long time;

        public long timeSum;

        public long timeSumOfSquares;

        public long count;
    }
}
