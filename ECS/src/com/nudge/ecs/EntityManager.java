package com.nudge.ecs;


import com.nudge.ecs.util.containers.Container;

/**
 * @author Frederik Dahl
 * 22/08/2021
 */


public class EntityManager {

    private final ECS ecs;

    private final Container<Entity> entities;
    private final Container<Entity> dirty;
    private final EntityPool pool;


    protected EntityManager(ECS ecs, int initialCapacity, int maxPoolSize) {
        this.ecs = ecs;
        entities = new Container<>(initialCapacity);
        dirty = new Container<>(initialCapacity);
        pool = new EntityPool(initialCapacity,maxPoolSize);
        pool.fill(initialCapacity/2);
    }



    protected void terminate() {
        entities.iterate(this::remove);
        clean();
        // entities and dirty should be empty atp.
        // so should all the systems' KVArrays
        if (entities.notEmpty() || dirty.notEmpty())
            throw new IllegalStateException("Temporary");
        pool.clear(false);
    }


    public Entity create() {
        Entity e = pool.obtain();
        entities.set(e,e.id());
        return e;
    }

    /**
     * Used to delete entities (return entities to pool).
     * This removes all components from the entity, and marks it dirty.
     * Dirty entities get "cleaned" after each EntitySystems' process-loop.
     * Dirty entities without components gets deleted.
     *
     * Note: If you remove an entity then add components to it within
     * the execution of the SAME EntitySystem, the entity will not get deleted.
     *
     * @param e the entity to remove
     */
    public void remove(Entity e) {
        if (e.hasAnyComponent())
            ecs.componentManager.removeAll(e);
        refresh(e);
    }

    public void addComponents(Entity e, Component... components) {
        boolean shouldRefresh = false;
        for (Component c : components) {
            if (ecs.componentManager.addComponent(e, c))
                shouldRefresh = true;
        }
        if (shouldRefresh) refresh(e);
    }

    public void addComponent(Entity e, Component c) {
        if (ecs.componentManager.addComponent(e,c))
            refresh(e);
    }

    public void removeComponent(Entity e, ComponentType t) {
        if (ecs.componentManager.removeComponent(e,t))
            refresh(e);
    }

    public void removeComponent(Entity e, Component c) {
        if (ecs.componentManager.removeComponent(e,c))
            refresh(e);
    }

    public void disable(Entity e) {
        if (e.isEnabled()) refresh(e);
        e.disable();
    }

    public void enable(Entity e) {
        if (!e.isEnabled()) refresh(e);
        e.enable();
    }

    /**
     * Adds the entity e to the dirty container.
     * Dirty entities gets "cleaned" (revalidated) after each EntitySystem loop, then removed from dirty.
     * Refreshing an entity without components will delete (free) the entity.
     *
     * @param e the entity to refresh
     */
    public void refresh(Entity e) {
        if (e.isDirty()) return;
        dirty.push(e);
        e.markAsDirty();
    }

    public int entities() {
        return entities.itemCount();
    }

    public long entitiesCreated() {
        return pool.obtained();
    }

    public int entitiesDestroyed() {
        return pool.discarded();
    }

    public int entitiesInMemory() {
        return pool.objectsInMemory();
    }

    /**
     * "Cleans" entities marked as dirty.
     * (Adding/removing components to/from an entity marks it as dirty)
     * This gets called at the beginning of each EntitySystems' process-loop.
     * Any dirty entities will get revalidated by each EntitySystem registered by the ECS.
     *
     * Note: Entities marked as dirty without components, will be deleted after clean.
     * Deleting an entity is equivalent of removing all it's components and vice-versa.
     */
    protected void clean() {  // make synchronized?
        if (dirty.notEmpty()) {
            final Container<ECSystem> systems = ecs.systemManager.systems;
            final int systemCount = systems.itemCount();
            final int dirtyCount = dirty.itemCount();
            for (int i = 0; i < dirtyCount; i++) {
                Entity e = dirty.get(i);
                for (int j = 0; j < systemCount; j++)
                    systems.get(j).revalidate(e);
                e.markAsClean();
                if (!e.hasAnyComponent())
                    delete(e);
            }dirty.clear();
        }
    }

    private void delete(Entity e) {
        entities.remove(e.id());
        pool.free(e);
    }

}
