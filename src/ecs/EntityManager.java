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
    private ComponentManager componentManager;

    private final IntStack freeIDs = new IntStack();
    private int genID = 0;

    private final Container<Entity> alive;
    private final Container<Entity> dirty;


    public EntityManager(ECS ecs, int initialCapacity, int maxPoolSize) {
        super(initialCapacity, maxPoolSize);
        alive = new Container<>(initialCapacity);
        dirty = new Container<>(initialCapacity);
        fill(initialCapacity/2);
        this.ecs = ecs;
    }



    // does not need to refresh if replacing existing component
    protected void addComponent(Entity e, Component c) {
        if (!componentManager.addComponent(e,c)) // if !replaced
            refresh(e);
    }

    protected void removeComponent(Entity e, ComponentType type) {
        // check if entity has component
    }

    protected void disable(Entity e) {
        if (e.enabled) refresh(e);
        e.enabled = false;
    }

    protected void enable(Entity e) {
        if (!e.enabled) refresh(e);
        e.enabled = true;
    }

    private void refresh(Entity e) {
        if (e.dirty) return;
        dirty.push(e);
        e.dirty = true;
    }

    @Override
    protected Entity newObject() {
        int id = freeIDs.isEmpty() ? genID : freeIDs.pop();
        return new Entity(id);
    }

    /**
     * Called when an Entity gets put back into the pool.
     * @param e The freed Entity
     */
    @Override
    protected void onObjectPooled(Entity e) {

        e.reset(); // just reset the bits here. No reason to have a method in entity for it. Unless entity need it internal.
    }

    /**
     * Called when an Entity gets discarded from the pool.
     * This happens when the pool is maxed. And instead of reset, discard is called.
     * @param e The discarded Entity
     */
    @Override
    protected void onObjectDiscarded(Entity e) {
        freeIDs.push(e.id());
    }

    public int entityCount() {
        return alive.itemCount();
    }
}
