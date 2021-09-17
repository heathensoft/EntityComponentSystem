package ecs.util.time;

import static java.lang.System.nanoTime;

/**
 *
 * Double precision timer, measured in seconds.
 * Used inside loops to trigger an update every [interval] seconds.
 * if abs(interval) = 0, it will be stuck in the while loop forever.
 *
 * Choose reasonable values for the interval. No shorter than the time of an update.
 *
 * There will naturally be spikes, and the DeltaLoop should be able to handle them:
 *
 * If the time of an update > 3 times the time of an interval, the
 * DeltaLoop will cap the delta time to max 3 times the interval.
 * This will prevent death spirals to some extent. Death spirals
 * being ever-accumulating delta time values due the updates taking to long.
 *
 * Inaccurate when the time of the actual update > chosen interval value.
 * Inaccurate for intervals < 0.001 seconds.
 *
 * You can use precision() to find the right value for the interval.
 *
 *
 * initialize() before a loop.
 * update() inside a loop.
 *
 * deltaLoop.initialize();
 * while(condition) { deltaLoop.update(); }
 *
 *
 *
 * @author Frederik Dahl
 * 16/09/2021
 */


public class DeltaLoop {

    private double runtime;
    private double interval;
    private double startTime;
    private double deltaTime;
    private double accumulator;
    private long updates;

    private final Update update;

    /**
     * Simulates 60 updates every second
     * @param update the update method implementation
     */
    public DeltaLoop(Update update) {
        this(update,1 / 60d);
    }
    /**
     *
     * @param update the update method implementation
     * @param interval the update interval in seconds
     */
    public DeltaLoop(Update update, double interval) {
        this.interval = Math.abs(interval);
        this.update = update;
    }

    public void initialize() {
        startTime = timeSeconds();
        accumulator = 0.0d;
        deltaTime = 0.0d;
    }

    public void update() {
        runtime += deltaTime;
        deltaTime = Math.min(deltaTime, interval * 3);
        accumulator += deltaTime;
        while (accumulator > interval) {
            updates++;
            accumulator -= interval;
            update.step(interval);}
        final double endTime = timeSeconds();
        deltaTime = endTime - startTime;
        startTime = endTime;
    }

    public void adjust(double interval) {
        this.interval = Math.abs(interval);
    }

    // derived from interval/(runtime/updates) to prevent zero-division
    public double precision() {
        return interval / ((runtime + interval) / (updates + 1));
    }

    public long updates() {
        return updates;
    }

    public double runTime() {
        return runtime;
    }

    private double timeSeconds() {
        return nanoTime() / 1_000_000_000.0d;
    }
}
