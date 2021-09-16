package ecs.util.time;

import static java.lang.System.nanoTime;

/**
 *
 * Double precision timer, measured in seconds.
 * Used inside loops to trigger an update every [interval] seconds.
 * if abs(interval) = 0, it will update forever.
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

    private double interval;
    private double startTime;
    private double deltaTime;
    private double accumulator;

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
        deltaTime = 0.0d;
        accumulator = 0.0d;
    }

    public void update() {
        accumulator += deltaTime;
        while (accumulator > interval) {
            accumulator -= interval;
            update.step(interval);
        }
        final double endTime = timeSeconds();
        deltaTime = endTime - startTime;
        startTime = endTime;
    }

    public void adjust(double interval) {
        this.interval = Math.abs(interval);
    }

    private double timeSeconds() {
        return nanoTime() / 1_000_000_000.0d;
    }
}
