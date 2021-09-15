package ecs;


import java.time.LocalDateTime;

import static java.lang.System.nanoTime;

/**
 *
 * Diagnostics are run from ECS class.
 * It keeps track of the thread running it.
 * The awaitTermination() method is equivalent to the Thread join() method
 * IT IS RECOMMENDED using a new Diagnostic instance for every "run"
 *
 *
 *
 * @author Frederik Dahl
 * 09/09/2021
 */


public abstract class Diagnostics implements Runnable{

    private Thread thread;
    private final double interval; // time of a "tick" in seconds
    private boolean isRunning;
    private boolean isSet;
    private RunTimeStatistics rts;

    public Diagnostics(double interval) {
        this.interval = interval;
    }

    @Override
    public void run() {

        synchronized (this) {
            if (thread != null | isRunning | !isSet)
                throw new IllegalStateException("");
            isRunning = true;
            this.thread = Thread.currentThread();
        }

        try {
            init(LocalDateTime.now());
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
            finish(LocalDateTime.now());
        }
    }

    private double timeSeconds() {
        return nanoTime() / 1_000_000_000.0d;
    }

    protected abstract void init(LocalDateTime timeStamp);

    protected abstract void process(long tick, RunTimeStatistics snapShot);

    protected abstract void finish(LocalDateTime timeStamp);

    protected final void set(RunTimeStatistics rts) {
        this.isSet = true;
        this.rts = rts;
    }

    public synchronized final void stop() {
        isRunning = false;
    }

    public synchronized final boolean isRunning() {
        return isRunning;
    }

    public synchronized final void awaitTermination()
            throws InterruptedException{
        if (this.thread == null) return;
        if (Thread.currentThread().equals(this.thread))
            throw new IllegalStateException("Infinite: join on itself");
        if (isRunning) stop();
        this.thread.join();
        this.thread = null;
    }
}
