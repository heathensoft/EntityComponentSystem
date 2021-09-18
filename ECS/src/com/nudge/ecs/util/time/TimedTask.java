package com.nudge.ecs.util.time;

/**
 * @author Frederik Dahl
 * 17/09/2021
 */


public abstract class TimedTask implements Runnable{

    double interval;
    double seconds;

    public TimedTask(double seconds, double interval) {
        this.seconds = seconds;
        this.interval = interval;
    }

    @Override
    public void run() {
        DeltaLoop loop = new DeltaLoop(this::update,interval);
        loop.initialize();
        while (loop.runTime() <= seconds)
            loop.update();
    }

    public abstract void update(double dt);
}
