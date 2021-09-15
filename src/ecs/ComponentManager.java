package ecs;

import ecs.util.Container;

import java.util.List;

/**
 *
 * Components are stored and queried here.
 *
 *      Components are indexed by type and entity-id.
 *      Component containers are not tightly stacked. But they are automatically
 *      refitted to save memory. This happens if a container for a specific
 *      component-type has been inactive for more than 5 minutes.
 *      The CapacityControl then checks if there is possible to save space.
 *      If it is, it will shrink that container to fit its outermost component.
 *
 * @author Frederik Dahl
 * 01/09/2021
 */


public class ComponentManager {

    protected final ECS ecs;
    protected final ComponentPools pools;
    protected final TypeManager typeManager;
    protected final CapacityControl control;

    private final Container<Container<Component>> components;

    private int active      = 0;    // components in play
    private long added      = 0L;   // total number of components added
    private long removed    = 0L;   // total number of components removed
    private long lost       = 0L;   // total number of components removed and NOT returned to a pool

    private int containerRefits = 0;
    private int poolRefits = 0;

    protected ComponentManager(ECS ecs) {
        this.ecs = ecs;
        pools = new ComponentPools(this);
        typeManager = new TypeManager(this);
        control = new CapacityControl(this);
        components = new Container<>(9); // 9 hits 64 (Max) on resizing
    }


    protected void update(float dt) {
        control.check(dt);
    }

    protected void terminate() {

    }

    protected <T extends Component> void registerPool(ComponentPool<T> pool, Class<T> clazz) {
        pools.register(pool,clazz);
    }

    /**
     * Any replaced component get pooled if pool is registered for type.
     * If the component replaced another of the same type. There is no need
     * to refresh the entity. (no need to revalidate it's belonging in systems)
     *
     * @param e the entity
     * @param c the component to be added to entity
     * @return whether the entity should be refreshed
     */
    protected boolean addComponent(Entity e, Component c) {
        if (c == null)
            throw new IllegalStateException("null Component");
        boolean shouldRefreshEntity;
        final ComponentType type = getType(c.getClass());
        final byte typeID = type.id();
        if (e.hasComponent(type.flag())) {
            final Component removed = removeComponentFromContainer(e.id(),typeID);
            if (removed == null) throw new IllegalStateException("Component should not be null atp");
            // explicitly typed for readability. see the tryFree() def.
            final boolean lostReference = pools.tryFree(c,type);
            if (lostReference) lost++;
            shouldRefreshEntity = false;
        } else {
            shouldRefreshEntity = true;
            e.addComponent(type.flag());
            control.resetContainerTimer(typeID);
            added++;
            active++;
        }
        components.get(typeID).set(c,e.id());
        return shouldRefreshEntity;
    }

    protected void removeAll(Entity e) {
        // entity is checked if it has any components before this method-call.
        // so we know the entity has at least one component atp
        // exceptions should be removed after testing stages
        Component c;
        Container<Component> byType;
        for (ComponentType t: typeManager.getList()) {
            if (!e.hasComponent(t.flag()))
                continue;
            byType = components.get(t.id());
            c = byType.remove(e.id());
            if (c == null)
                throw new IllegalStateException("Component should not be null atp");
            e.removeComponent(t.flag());
            removed++;
            active--;
            control.resetContainerTimer(t.id());
            // explicitly typed for readability. see the tryFree() def.
            final boolean lostReference = pools.tryFree(c,t);
            if (lostReference) lost++;
        }
    }

    /**
     * Removes the ComponentType from the entity if it has one.
     * If no component was removed there is no need to refresh the entity.
     * Tries to free the component (return it to pool) if the type has a registered pool.
     * @param e the entity
     * @param t the type of component to remove
     * @return whether the component was found and the entity should refresh.
     */
    protected boolean removeComponent(Entity e, ComponentType t) {
        if (!e.hasComponent(t.flag())) return false;
        final byte typeID = t.id();
        final Component c = removeComponentFromContainer(e.id(),typeID);
        if (c == null) // if no component found, but entity's flag is true
            throw new IllegalStateException("Component should not be null atp");
        e.removeComponent(t.flag());
        // explicitly typed for readability. see the tryFree() def.
        final boolean lostReference = pools.tryFree(c,t);
        if (lostReference) lost++;
        control.resetContainerTimer(typeID);
        removed++;
        active--;
        return true;
    }

    /**
     * Removes a Component of the same type as c from the entity.
     * What matters is the type of c. It does not check for equals.
     * It could remove another component entirely (of the same type) than the passed in c.
     * Removed components get pooled if pool registered for type.
     * @param e the entity
     * @param c the component to be removed (Only the type of c matters)
     * @return whether a component was removed and the entity therefore should be refreshed
     */
    protected boolean removeComponent(Entity e, Component c) {
        return removeComponent(e,getType(c.getClass()));
    }

    private Component removeComponentFromContainer(int entityID, byte typeID) {
       final Container<Component> container = components.get(typeID);
        if (entityID < container.usedSpace())
            return container.remove(entityID);
        return null;
    }

    /**
     * Unsafe. Does not check for index outOfBounds.
     * see: ecs.Getter.java. The only class that use this.
     * public equivalent: getComponent()
     *
     * @param entityID the entity
     * @param typeID the type
     * @return the component
     */
    protected Component getComponentUnsafe(int entityID, byte typeID) {
        return components.get(typeID).get(entityID);
    }

    public Component getComponent(Entity e, ComponentType t) {
        return getComponent(e.id(), t.id());
    }

    protected Component getComponent(int entityID, byte typeID) {
        final Container<Component> byType = components.get(typeID);
        if (entityID < byType.usedSpace())
            return byType.get(entityID);
        return null;
    }


    // Type getter/creators - public in ECS

    protected ComponentType getType(Class<? extends Component> c) {
        return typeManager.getType(c);
    }

    protected ComponentGroup getGroup(ComponentType... types) {
        return this.typeManager.getGroup(types);
    }

    @SafeVarargs
    protected final ComponentGroup getGroup(Class<? extends Component>... classes) {
       return typeManager.getGroup(classes);
    }



    // Stats - public in RunTimeStatistics

    protected int componentsActive() {
        return active;
    }

    protected long componentsAdded() {
        return added;
    }

    protected long componentsRemoved() {
        return removed;
    }

    protected long componentsLost() {
        return lost;
    }

    protected int containerRefits() {
        return containerRefits;
    }

    protected int poolRefits() {
        return poolRefits;
    }




    protected Container<Component> getContainer(ComponentType t) {
        if (t == null) // container will always exist if type exist.
            throw new IllegalStateException("ComponentType = null");
        return components.get(t.id());
    }

    protected int getContainerSize(ComponentType t) {
        return components.get(t.id()).itemCount();
    }

    protected int getContainerCapacity(ComponentType t) {
        return components.get(t.id()).capacity();
    }

    protected float getContainerLoadFactor(ComponentType t) {
        return components.get(t.id()).loadFactor();
    }

    protected float getContainersLoadFactor() {
        final List<ComponentType> typesList = typeManager.getList();
        if (typesList.isEmpty())
            return 1f;
        int capacity = 0;
        int count = 0;
        for (ComponentType type: typesList) {
            capacity += getContainerCapacity(type);
            count += getContainerSize(type);
        }
        // capacity would never be zero
        return (float) count / capacity;
    }




    // Callbacks

    // callback from ComponentTypes
    protected void newContainer() {
        components.push(new Container<>());
    }

    // callback from ContainerControl
    protected void attemptRefitContainer(byte typeID) {
        if (components.get(typeID).fit(false))
            containerRefits++;
    }

    // callback from ContainerControl
    protected void attemptRefitPool(byte typeID) {
        if (pools.getPools().get(typeID).fit())
            poolRefits++;
    }

}
