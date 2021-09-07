package ecs;

/**
 * @author Frederik Dahl
 * 07/09/2021
 */


public abstract class ECSManager {

    protected void set(ECS ecs) {}
    protected void initialize() {}
    protected abstract void terminate();
}
