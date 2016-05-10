/*
 * Copyright (c) 2016 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind;

public class FrameMetrics {

    private final Object drawLock = new Object();

    protected Metrics renderMetrics = new Metrics();

    protected Metrics drawMetrics = new Metrics();

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

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("FrameMetrics");
        sb.append("{\n");
        sb.append("renderTime=").append(this.renderMetrics.time);
        sb.append(", renderTimeTotal=").append(this.renderMetrics.timeSum);
        sb.append(", renderCount=").append(this.renderMetrics.count);
        sb.append(", renderTimeAvg=").append(String.format("%.1f", this.computeTimeAverage(this.renderMetrics)));
        sb.append(", renderTimeStdDev=").append(String.format("%.1f", this.computeTimeStdDev(this.renderMetrics)));
        sb.append("\n");

        synchronized (this.drawLock) {
            sb.append("drawTime=").append(this.drawMetrics.time);
            sb.append(", drawTimeTotal=").append(this.drawMetrics.timeSum);
            sb.append(", drawCount=").append(this.drawMetrics.count);
            sb.append(", drawTimeAvg=").append(String.format("%.1f", this.computeTimeAverage(this.drawMetrics)));
            sb.append(", drawTimeStdDev=").append(String.format("%.1f", this.computeTimeStdDev(this.drawMetrics)));
        }

        sb.append("\n}");

        return sb.toString();
    }

    public void beginRendering() {
        long now = System.currentTimeMillis();

        this.markBegin(this.renderMetrics, now);
    }

    public void endRendering() {
        long now = System.currentTimeMillis();
        this.markEnd(this.renderMetrics, now);
    }

    public void beginDrawing() {
        long now = System.currentTimeMillis();

        synchronized (this.drawLock) {
            this.markBegin(this.drawMetrics, now);
        }
    }

    public void endDrawing() {
        long now = System.currentTimeMillis();

        synchronized (this.drawLock) {
            this.markEnd(this.drawMetrics, now);
        }
    }

    public void reset() {
        this.resetMetrics(this.renderMetrics);

        synchronized (this.drawLock) {
            this.resetMetrics(this.drawMetrics);
        }
    }

    protected void markBegin(Metrics metrics, long timeMillis) {
        metrics.begin = timeMillis;
    }

    protected void markEnd(Metrics metrics, long timeMillis) {
        metrics.time = timeMillis - metrics.begin;
        metrics.timeSum += metrics.time;
        metrics.timeSumOfSquares += (metrics.time * metrics.time);
        metrics.count++;
    }

    protected void resetMetrics(Metrics metrics) {
        // reset the metrics collected across multiple frames
        metrics.timeSum = 0;
        metrics.timeSumOfSquares = 0;
        metrics.count = 0;
    }

    protected double computeTimeAverage(Metrics metrics) {
        return metrics.timeSum / (double) metrics.count;
    }

    protected double computeTimeStdDev(Metrics metrics) {
        double avg = (double) metrics.timeSum / (double) metrics.count;
        double var = ((double) metrics.timeSumOfSquares / (double) metrics.count) - (avg * avg);
        return Math.sqrt(var);
    }

    protected static class Metrics {

        public long begin;

        public long time;

        public long timeSum;

        public long timeSumOfSquares;

        public long count;
    }
}
