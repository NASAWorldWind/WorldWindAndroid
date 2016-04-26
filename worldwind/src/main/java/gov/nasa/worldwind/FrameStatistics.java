/*
 * Copyright (c) 2016 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind;

public class FrameStatistics {

    private final Object lock = new Object(); // TODO remove lock and associated synchronization

    protected long frameTime;

    protected long frameTimeSum;

    protected long frameTimeSumOfSquares;

    protected long frameCount;

    protected long frameBegin;

    public FrameStatistics() {
    }

    public long getFrameTime() {
        synchronized (this.lock) {
            return this.frameTime;
        }
    }

    public double getFrameTimeAverage() {
        synchronized (this.lock) {
            return this.frameTimeSum / (double) this.frameCount;
        }
    }

    public double getFrameTimeStdDev() {
        synchronized (this.lock) {
            double avg = (double) this.frameTimeSum / (double) this.frameCount;
            double var = ((double) this.frameTimeSumOfSquares / (double) this.frameCount) - (avg * avg);
            return Math.sqrt(var);
        }
    }

    public long getFrameTimeTotal() {
        synchronized (this.lock) {
            return this.frameTimeSum;
        }
    }

    public long getFrameCount() {
        synchronized (this.lock) {
            return this.frameCount;
        }
    }

    @Override
    public String toString() {
        synchronized (this.lock) {
            return String.format(
                "FrameStatistics{frameTime=%d, frameTimeAverage=%.1f, frameTimeStdDev=%.1f, frameTimeTotal=%d, frameCount=%d",
                this.frameTime,
                this.getFrameTimeAverage(),
                this.getFrameTimeStdDev(),
                this.frameTimeSum,
                this.frameCount);
        }
    }

    public void beginFrame() {
        synchronized (this.lock) {
            this.frameBegin = System.currentTimeMillis();
        }
    }

    public void endFrame() {
        synchronized (this.lock) {
            long now = System.currentTimeMillis();
            this.frameTime = now - this.frameBegin;
            this.frameTimeSum += this.frameTime;
            this.frameTimeSumOfSquares += (this.frameTime * this.frameTime);
            this.frameCount++;
        }
    }

    public void reset() {
        synchronized (this.lock) {
            this.frameTime = 0;
            this.frameTimeSum = 0;
            this.frameTimeSumOfSquares = 0;
            this.frameCount = 0;
            this.frameBegin = 0;
        }
    }
}
