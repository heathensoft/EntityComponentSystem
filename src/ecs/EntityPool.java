package ecs;

import ecs.util.IntStack;
import ecs.util.containers.Pool;

/**
 *
 * The Entity pool supplies the ECS with reusable entities.
 *
 * @author Frederik Dahl
 * 07/09/2021
 */


public class EntityPool extends Pool<Entity> {

    private final IntStack freeIDs = new IntStack();
    private int genID = 0;

    protected EntityPool(int initialCapacity, int max) {
        super(initialCapacity, max);
    }

    @Override
    protected void reset(Entity e) {
        e.reset();
    }

    @Override
    protected void discard(Entity e) {
        freeIDs.push(e.id());
    }

    @Override
    protected Entity newObject() {
        int id = freeIDs.isEmpty() ? genID++ : freeIDs.pop();
        return new Entity(id);
    }
}
