package com.nudge.ecs;


import com.nudge.ecs.util.IntQueue;
import com.nudge.ecs.util.IntStack;
import com.nudge.ecs.util.containers.Pool;

/**
 *
 * The Entity pool supplies the ECS with reusable entities.
 *
 * @author Frederik Dahl
 * 07/09/2021
 */


public class EntityPool extends Pool<Entity> {

    private final IntQueue freeIDs = new IntQueue();
    private int genID = 0;

    protected EntityPool(int initialCapacity) {
        super(initialCapacity, Short.MAX_VALUE);
    }

    @Override
    protected void reset(Entity e) {
        e.reset();
    }

    @Override
    protected void discard(Entity e) {
        freeIDs.enqueue(e.id());
    }

    @Override
    protected Entity newObject() {
        int id = freeIDs.isEmpty() ? genID++ : freeIDs.dequeue();
        return new Entity(id);
    }
}
