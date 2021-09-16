package ecs;


import ecs.util.time.DeltaLoop;
import ecs.util.time.Update;

import java.time.LocalDateTime;

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

    private Thread thread;
    private final double interval;
    private boolean running;
    private boolean set;
    private RunTimeStatistics rts;

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
            running = true;
            this.thread = Thread.currentThread();
        }

        DeltaLoop deltaLoop = new DeltaLoop(new Update() {
            private long tick = 0L;
            @Override
            public void step(double deltaTime) {
                process(++tick,rts);
            }
        }, interval);

        Exception e = null;

        try {
            start(LocalDateTime.now());
            deltaLoop.initialize();
            while (running) deltaLoop.update();
        }catch (Exception exception) {
            stop();
            e = exception;
        }finally {
            finish(LocalDateTime.now(),e);
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
    public synchronized void stop() {
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
