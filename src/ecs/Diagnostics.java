package ecs;


import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static java.lang.System.nanoTime;

/**
 *
 * Diagnostics are run from ECS class.
 * It keeps track of the thread running it.
 * The awaitTermination() method is like the Thread join() method,
 * only you must call stop() beforehand.
 * If you run the same Diagnostic instance twice,
 * i.e. Diagnostic is running, and you invoke the same instance twice in ecs.run(diagnostic)
 * then a new thread is started (after the old is terminated) and you are responsible
 * for resetting the necessary data. This can be done in the finish() method.
 * Even if you did not entirely understand this, either way:
 * IT IS BETTER PRACTICE using a new diagnostic object for every "run"
 *
 *
 *
 * @author Frederik Dahl
 * 09/09/2021
 */


public abstract class Diagnostics implements Runnable{

    /*
    final StringBuilder path = new StringBuilder();
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyMMddHHmm");
    path.append(directory).append("ECS-D_").append(LocalDateTime.now().format(formatter)).append(".csv");
     */

    private Thread thread;
    private final double interval; // time of a "tick" in seconds
    private volatile boolean isRunning;
    private boolean isSet;
    private RunTimeStatistics rts;
    private final Object lock = new Object();
    private String id = "";

    public Diagnostics(double interval) {
        this.interval = interval;
    }

    @Override
    public void run() {
        synchronized (lock) {
            if (thread == null | isRunning | !isSet) {
                throw new IllegalStateException("");
            }
            isRunning = true;
            this.thread = Thread.currentThread();
        }
        try {
            setID();
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
            finish();
        }
    }

    private void setID() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyMMddHHmm");
        id = "ECS-D_" + LocalDateTime.now().format(formatter);
    }

    private double timeSeconds() {
        return nanoTime() / 1_000_000_000.0d;
    }

    protected abstract void process(long tick, RunTimeStatistics snapShot);

    protected abstract void finish();

    protected final void set(RunTimeStatistics rts) {
        this.rts = rts;
        isSet = true;
    }

    public String id() {
        return id;
    }

    public final void stop() {
        isRunning = false;
    }

    public synchronized final void awaitTermination()
            throws InterruptedException{
        if (Thread.currentThread() == thread)
            throw new IllegalStateException("Attempted to join with itself");
        if (isRunning)
            throw new IllegalStateException("Always invoke stop before this");
        if (thread == null) return;
        if (thread.getState() != Thread.State.TERMINATED)
            thread.join();
        thread = null;
    }
}
