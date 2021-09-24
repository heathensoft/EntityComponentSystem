package com.nudge.ecs;


import com.nudge.ecs.util.containers.Container;
import com.nudge.ecs.util.containers.Pool;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Frederik Dahl
 * 13/09/2021
 */


public class ComponentPools {

    private final ComponentManager manager;
    private final Container<ComponentPool<? extends Component>> pools;
    private final List<ComponentType> types;
    private long poolFlags = 0L;

    protected ComponentPools(ComponentManager componentManager) {
        this.manager = componentManager;
        this.pools = new Container<>(9);
        this.types = new ArrayList<>();
    }


    protected <T extends Component> void register(ComponentPool<T> pool, Class<T> clazz) {
        ComponentType type = manager.getType(clazz);
        if (poolRegistered(type))
            throw new IllegalStateException("Pool already registered for type: " + type.name());
        occupyPoolSlot(type);
        types.add(type);
        pools.set(pool,type.id());
        pool.register(manager.control,type);
    }

    /**
     * Called on ComponentManager Termination. Will clear all pools, nullifying components.
     * This is done after all entities are deleted.
     */
    protected void clearItems() {
        pools.iterate(Pool::clear);

    }

    /**
     * Nullifies the pool containers
     */
    protected void nullifyPools() {
        pools.clear();
    }

    /**
     * Tries to return the component to a pool if the type has one.
     * It returns true if the component was "lost".
     * Meaning, there was no attempt at pooling it and therefore any
     * ECS-internal references to it are lost.
     *
     * @param c the component
     * @param type the component type
     * @return true if the component type does not have a registered pool
     */
    protected boolean tryFree(Component c, ComponentType type) {
        if (poolRegistered(type)) {
            pools.get(type.id()).freeInternal(c);
            return false;
        } return true;
    }

    protected Container<ComponentPool<? extends Component>> getPools() {
        return pools;
    }

    protected ComponentPool<? extends Component> getPool(ComponentType type) {
        if (poolRegistered(type))
            return pools.get(type.id());
        return null;
    }

    protected int inPool(ComponentType type) {
        ComponentPool<? extends Component> pool;
        pool = getPool(type);
        if (pool == null)
            return 0;
        return pool.size();
    }

    protected int capacity(ComponentType type) {
        ComponentPool<? extends Component> pool;
        pool = getPool(type);
        if (pool == null)
            return 0;
        return pool.capacity();
    }

    protected int discarded(ComponentType type) {
        ComponentPool<? extends Component> pool;
        pool = getPool(type);
        if (pool == null)
            return 0;
        return pool.discarded();
    }

    protected long obtained(ComponentType type) {
        ComponentPool<? extends Component> pool;
        pool = getPool(type);
        if (pool == null)
            return 0;
        return pool.obtained();
    }

    protected float loadFactor(ComponentType type) {
        ComponentPool<? extends Component> pool;
        pool = getPool(type);
        if (pool == null)
            return 0;
        return pool.loadFactor();
    }

    protected int inPoolTotal() {
        if (types.isEmpty())
            return 0;
        int count = 0;
        for (ComponentType type: types)
            count += pools.get(type.id()).size();
        return count;
    }

    protected long obtainedTotal() {
        if (types.isEmpty())
            return 0;
        long count = 0;
        for (ComponentType type: types)
            count += pools.get(type.id()).obtained();
        return count;
    }

    protected long discardedTotal() {
        if (types.isEmpty())
            return 0;
        long count = 0;
        for (ComponentType type: types)
            count += pools.get(type.id()).discarded();
        return count;
    }

    protected float loadFactorAll() {
        if (types.isEmpty())
            return 1f;
        int capacity = 0;
        int count = 0;
        ComponentPool<? extends Component> pool;
        for (ComponentType type: types) {
            pool = pools.get(type.id());
            capacity += pool.capacity();
            count += pool.size();
        }
        // capacity would never be zero
        return (float) count / capacity;
    }

    protected int poolCount() {
        return pools.itemCount();
    }


    private boolean poolRegistered(ComponentType type) {
        return (poolFlags & type.flag()) == type.flag();
    }

    private void occupyPoolSlot(ComponentType type) {
        poolFlags |= type.flag();
    }
}
