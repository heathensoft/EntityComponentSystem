package ecs;

import ecs.util.Container;
import ecs.util.Pool;
import ecs.util.IntStack;


/**
 * @author Frederik Dahl
 * 22/08/2021
 */


public class EntityManager extends Pool<Entity> {

    private final ECS ecs;

    private final IntStack freeIDs = new IntStack();
    private int genID = 0;
    protected int newCount = 0;
    protected int discardCount = 0; // can have this in pool
    protected int disabledCount = 0;

    private final Container<Entity> alive;
    private final Container<Entity> dirty;


    public EntityManager(ECS ecs, int initialCapacity, int maxPoolSize) {
        super(initialCapacity, maxPoolSize);
        alive = new Container<>(initialCapacity);
        dirty = new Container<>(initialCapacity);
        fill(initialCapacity/2);
        this.ecs = ecs;
    }






    protected void addToDirty(Entity e) {

    }


    @Override
    protected Entity newObject() {
        int id = freeIDs.isEmpty() ? genID : freeIDs.pop();
        newCount++; // have this in pool
        return new Entity(this,ecs.componentManager,id);
    }

    /**
     * Called when an Entity gets put back into the pool.
     * @param e The freed Entity
     */
    @Override
    protected void reset(Entity e) {

        e.reset(); // just reset the bits here. No reason to have a method in entity for it. Unless entity need it internal.
    }

    /**
     * Called when an Entity gets discarded from the pool.
     * This happens when the pool is maxed. And instead of reset, discard is called.
     * @param e The discarded Entity
     */
    @Override
    protected void discard(Entity e) {
        freeIDs.push(e.id());
        discardCount++;
    }
}
