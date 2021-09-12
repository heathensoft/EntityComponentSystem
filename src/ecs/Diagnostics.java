package ecs;

import static java.lang.System.nanoTime;

/**
 * @author Frederik Dahl
 * 09/09/2021
 */


public abstract class Diagnostics implements Runnable{

    private final double interval; // time of a "tick" in seconds
    private volatile boolean isRunning;
    private boolean isSet;
    private RunTimeStatistics rts;

    public Diagnostics(double interval) {
        this.interval = interval;
    }

    @Override
    public void run() {
        synchronized (this) {
            if (isRunning | !isSet)
                return;
            isRunning = true;
        }
        try {
            double startTime = timeSeconds();
            double endTime;
            double deltaTime = 0.0d;
            double accumulator = 0.0d;
            long tick = 0;
            while (isRunning) {
                accumulator += deltaTime;
                if (accumulator > interval) {
                    accumulator -= interval;
                    process(++tick,rts);
                }
                endTime = timeSeconds();
                deltaTime = endTime - startTime;
                startTime = endTime;
            }
        } finally {
            finish(rts);
        }
    }

    private double timeSeconds() {
        return nanoTime() / 1_000_000_000.0d;
    }

    protected abstract void process(long tick, RunTimeStatistics snapShot);

    protected abstract void finish(RunTimeStatistics finalState);

    protected final void set(RunTimeStatistics rts) {
        this.rts = rts;
        isSet = true;
    }

    public final void stop() {
        isRunning = false;
    }
}
