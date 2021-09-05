package ecs;

import ecs.util.Container;
import ecs.util.Pool;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * ComponentType and ComponentGroup instances are created and stored here.
 *
 *      Maximum number of unique ComponentTypes: 64 (Might be reduced to int32)
 *      Maximum number of unique ComponentGroups: 32767
 *      You cannot delete types or groups from a manager instance once created.
 *      When you initiate a type, 1 "slot" gets taken.
 *      When you initiate a group, it automatically creates types of members.
 *      You can rename types and groups. .ie someGroup.setName()
 *
 * Components are stored and queried here.
 *
 *      Components are indexed by type and entity -id.
 *      Component containers are not tightly stacked. But they are automatically
 *      refitted to save memory. This happens if a container for a specific
 *      component-type has been inactive for more than 5 minutes.
 *      The MemoryManager then checks if there is possible to save space.
 *      If it is, it will shrink that container to fit its outermost component.
 *
 * @author Frederik Dahl
 * 01/09/2021
 */


public class ComponentManager {

    // todo: remember to pool components that needs pooling (removal)
    //  I think poolTimers can be managed by the pool itself

    private final MemoryManager memoryManager;

    private final Map<Class<? extends Component>,ComponentType> types;
    private long nextFlag = 1L;
    private byte genTypeID = 0;

    private final Container<ComponentGroup> groups;
    private short genGroupID = 0;

    private final Container<Container<Component>> components;

    private final Container<ComponentPool<? extends Component>> pools;
    private long poolFlags = 0L;

    private int componentCount = 0;

    protected ComponentManager(MemoryManager memoryManager) {
        types = new HashMap<>();
        groups = new Container<>();
        components = new Container<>(9); // 9 will eventually hit 64 (Max) on resizing
        pools = new Container<>(9);
        this.memoryManager = memoryManager;
    }

    public <T extends Component> void addPool(ComponentPool<T> pool, Class<T> clazz) {
        ComponentType type = getType(clazz);
        if (poolExist(type))
            throw new IllegalStateException("Duplicate pool");
        occupyPoolSlot(type);
        pool.register(memoryManager,type);
        pools.set(pool,type.id);
    }

    // returns if a component needs to be refreshed
    // does not need to notify the memoryManager if it replaced another component
    protected boolean addComponent(Entity e, Component c) {
        boolean replaced;
        ComponentType type = getType(c.getClass());
        if (e.hasComponent(type.flag)) {
            Component removed = removeComponentFromContainer(e.id,type.id);
            if (removed == null) throw new IllegalStateException("Component should not be null atp");
            if (poolExist(type)) // if pool is registered for component type;
                pools.get(type.id).freeInternal(c);
            replaced = true;
        } else {
            replaced = false;
            e.addComponent(type.flag);
            e.enableComponent(type.flag);
            memoryManager.resetContainerTimer(type.id);
            componentCount++; // remember to decrease on removal
        }
        components.get(type.id).set(c,e.id);
        return replaced;
    }

    private Component removeComponentFromContainer(int entityID, byte typeID) {
        Container<Component> container = components.get(typeID);
        if (entityID < container.usedSpace())
            return container.remove(entityID);
        return null;
    }

    protected Component removeComponent(Entity e, ComponentType t) {
        Container<Component> container = components.get(t.id);
        Component c = null;
        int index = e.id;
        if (index < container.usedSpace()){
            c = container.remove(index);
            if (c != null) memoryManager.resetContainerTimer(t.id);
        }return c;
    }

    // presuppose the entity has a component of this type
    protected Component removeComponentUnsafe(Entity e, ComponentType t) {
        memoryManager.resetContainerTimer(t.id);
        return components.get(t.id).remove(e.id);
    }

    protected Component getComponentUnsafe(Entity e, ComponentType t) {
        return components.get(t.id).get(e.id);
    }

    protected Component getComponentUnsafe(int entity, byte type) {
        return components.get(type).get(entity);
    }

    public Component getComponent(Entity e, ComponentType t) {
        Container<Component> container = components.get(t.id);
        int index = e.id; // can remove index when id is protected
        if (index < container.usedSpace())
            return container.get(index);
        return null;
    }

    public long getTypeFlag(Component c) {
        return getType(c.getClass()).flag();
    }

    public ComponentType getType(Class<? extends Component> c) {
        ComponentType type = types.get(c);
        if (type == null) {
            if (uniqueTypes() == Long.SIZE)
                throw new IllegalStateException("limit break: maximum unique component-types");
            type = new ComponentType(c,nextFlag,genTypeID);
            nextFlag = nextFlag << 1;
            genTypeID++;
            types.put(c,type);
            components.push(new Container<>());
            memoryManager.newTimer();
        }return type;
    }

    public ComponentGroup getGroup(ComponentType... types) {
        ComponentGroup group;
        long mask = 0;
        for (ComponentType type : types)
            mask |= type.flag;
        group = lookUpGroup(mask);
        if (group == null) {
            group = new ComponentGroup(mask,genGroupID);
            groups.set(group,group.id);
            genGroupID++;
        }return group;
    }

    @SafeVarargs
    public final ComponentGroup getGroup(Class<? extends Component>... classes) {
        ComponentType[] types = new ComponentType[classes.length];
        for (int i = 0; i < classes.length; i++)
            types[i] = getType(classes[i]);
        return getGroup(types);
    }

    public int uniqueTypes() {
        return types.size();
    }

    public int uniqueGroups() {
        return groups.itemCount();
    }

    protected boolean autoFitContainer(byte index) {
        return components.get(index).fit(false);
    }

    protected boolean autoFitPool(byte index) {
        return pools.get(index).fit();
    }

    protected int poolCount() {
        return pools.itemCount();
    }

    protected int componentCount() {
        return componentCount;
    }

    protected Container<ComponentPool<? extends Component>> getPools() {
        return pools;
    }

    private ComponentGroup lookUpGroup(long mask) {
        ComponentGroup group = null;
        for (int i = 0; i < groups.itemCount(); i++) {
            group = groups.get(i);
            if (group.match(mask)) break;
        }return group;
    }

    private boolean poolExist(ComponentType type) {
        return (poolFlags & type.flag) == type.flag;
    }

    private void occupyPoolSlot(ComponentType type) {
        poolFlags |= type.flag;
    }
}
