/*
 * Copyright (c) 2016 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.render;

public class FrameStatistics {

    protected long frameTime;

    protected long frameTimeSum;

    protected long frameTimeSumOfSquares;

    protected long frameCount;

    protected long frameBegin;

    public FrameStatistics() {
    }

    public long getFrameTime() {
        return this.frameTime;
    }

    public double getFrameTimeAverage() {
        return this.frameTimeSum / (double) this.frameCount;
    }

    public double getFrameTimeStdDev() {
        double avg = (double) this.frameTimeSum / (double) this.frameCount;
        double var = ((double) this.frameTimeSumOfSquares / (double) this.frameCount) - (avg * avg);
        return Math.sqrt(var);
    }

    public long getFrameTimeTotal() {
        return this.frameTimeSum;
    }

    public long getFrameCount() {
        return this.frameCount;
    }

    @Override
    public String toString() {
        return String.format(
            "FrameStatistics{frameTime=%d, frameTimeAverage=%.1f, frameTimeStdDev=%.1f, frameTimeTotal=%d, frameCount=%d",
            this.frameTime,
            this.getFrameTimeAverage(),
            this.getFrameTimeStdDev(),
            this.frameTimeSum,
            this.frameCount);
    }

    public void beginFrame() {
        this.frameBegin = System.currentTimeMillis();
    }

    public void endFrame() {
        long now = System.currentTimeMillis();
        this.frameTime = now - this.frameBegin;
        this.frameTimeSum += this.frameTime;
        this.frameTimeSumOfSquares += (this.frameTime * this.frameTime);
        this.frameCount++;
    }

    public void reset() {
        this.frameTime = 0;
        this.frameTimeSum = 0;
        this.frameTimeSumOfSquares = 0;
        this.frameCount = 0;
        this.frameBegin = 0;
    }
}
