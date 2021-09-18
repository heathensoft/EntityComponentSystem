package com.nudge.ecs;


import com.nudge.ecs.util.time.DeltaLoop;
import com.nudge.ecs.util.time.Update;

import java.time.LocalDateTime;

import static java.time.LocalDateTime.now;

/**
 *
 * Diagnostics are run from ECS class (After setting it)
 * It keeps track of the thread running it.
 * The awaitTermination() method is equivalent exiting the loop and calling the Thread join() method
 * IT IS RECOMMENDED using a new Diagnostic instance for every "run"
 *
 *
 *
 * @author Frederik Dahl
 * 09/09/2021
 */


public abstract class Diagnostics implements Runnable {

    private RunTimeStatistics rts;
    private Thread thread;
    private final double interval;
    private boolean running;
    private boolean set;

    /**
     * @param interval the time-value in seconds of a "tick"
     */
    public Diagnostics(double interval) {
        this.interval = interval;
    }

    @Override
    public void run() {

        synchronized (this) {
            if (thread != null | running | !set)
                throw new IllegalStateException("");
            thread = Thread.currentThread();
            running = true;
        }
        DeltaLoop loop = new DeltaLoop(
                new Update() {
                    long tick = 0L;
                    public void step(double dt) {
                        process(++tick,rts);
                    }},
                interval);

        Exception e = null;
        try {start(now());
            loop.initialize();
            while (running)
                loop.update();
        }catch (Exception x) {
            stop();
            e = x;
        }finally {
            finish(now(),e);
        }
    }

    /**
     * This can be used to set up output and creating files.
     *
     * @param timeStamp the localDateTime. Useful for naming files
     * @throws Exception typically IOException. Exceptions are caught, and passed to finish()
     */
    protected abstract void start(LocalDateTime timeStamp) throws Exception;

    /**
     * Every tick the runTime snapshot is passed in here together with the tick value.
     * Used to process that information. Typically, output.
     *
     * @param tick the tick (number of intervals)
     * @param snapShot the RunTimeStatistics at the given time
     */
    protected abstract void process(long tick, RunTimeStatistics snapShot);

    /**
     * Handle tidy-ups here. i.e. flushing output buffers
     *
     * @param timeStamp the localDateTime
     * @param exception if null, diagnostics finished without exception
     */
    protected abstract void finish(LocalDateTime timeStamp, Exception exception);

    protected final void set(RunTimeStatistics rts) {
        this.set = true;
        this.rts = rts;
    }

    /**
     * Not used by the ECS
     */
    protected synchronized void stop() {
        running = false;
    }

    public synchronized final void awaitStop()
            throws InterruptedException{
        if (this.thread == null) return;
        if (Thread.currentThread().equals(this.thread))
            throw new IllegalStateException("Infinite: join on itself");
        stop();
        this.thread.join();
        this.thread = null;
    }
}
