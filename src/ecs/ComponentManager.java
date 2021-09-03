package ecs;

import ecs.util.Container;

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

    private final ECS ecs;

    private final Map<Class<? extends Component>,ComponentType> types;
    private long nextFlag = 1L;
    private byte genTypeID = 0;

    private final Container<ComponentGroup> groups;
    private short genGroupID = 0;

    private final Container<Container<Component>> components;

    private final Container<ComponentPool<? extends Component>> pools;
    private long poolFlags = 0L;

    protected int discardCount = 0;

    protected ComponentManager(ECS ecs) {
        this.ecs = ecs;
        types = new HashMap<>();
        groups = new Container<>();
        components = new Container<>(9); // 9 will eventually hit 64 (Max) on resizing
        pools = new Container<>(9);
    }

    public <T extends Component> void addPool(ComponentPool<T> pool, Class<T> clazz) {
        ComponentType type = getType(clazz);
        if (!isPoolSlotAvailable(type))
            throw new IllegalStateException("Duplicate ComponentType pools");
        occupyPoolSlot(type);
        pool.initialize(this,type);
        pools.set(pool,type.id());

        // make memory manager aware of pool
    }

    protected void addComponent(Entity e, Component c) {
        byte typeID = getType(c.getClass()).id();
        components.get(typeID).set(c,e.id());
        ecs.memoryManager.resetContainerTimer(typeID);
    }

    protected Component removeComponent(Entity e, ComponentType t) {
        Container<Component> container = components.get(t.id());
        Component c = null;
        int index = e.id();
        if (index < container.usedSpace()){
            c = container.remove(index);
            if (c != null) ecs.memoryManager.resetContainerTimer(t.id());
        }return c;
    }

    // presuppose the entity has a component of this type
    protected Component removeComponentUnsafe(Entity e, ComponentType t) {
        ecs.memoryManager.resetContainerTimer(t.id());
        return components.get(t.id()).remove(e.id());
    }

    protected Component getComponentUnsafe(Entity e, ComponentType t) {
        return components.get(t.id()).get(e.id());
    }

    protected Component getComponentUnsafe(int entity, byte type) {
        return components.get(type).get(entity);
    }

    public Component getComponent(Entity e, ComponentType t) {
        Container<Component> container = components.get(t.id());
        int index = e.id();
        if (index < container.usedSpace())
            return container.get(index);
        return null;
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
            ecs.memoryManager.addTimer();
        }return type;
    }

    public ComponentGroup getGroup(ComponentType... types) {
        ComponentGroup group;
        long mask = 0;
        for (ComponentType type : types)
            mask |= type.flag();
        group = lookUpGroup(mask);
        if (group == null) {
            group = new ComponentGroup(mask,genGroupID);
            groups.set(group,group.id());
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



    private ComponentGroup lookUpGroup(long mask) {
        ComponentGroup group = null;
        for (int i = 0; i < groups.itemCount(); i++) {
            group = groups.get(i);
            if (group.match(mask)) break;
        }return group;
    }

    private boolean isPoolSlotAvailable(ComponentType type) {
        return (poolFlags & type.flag()) == 0;
    }

    private void occupyPoolSlot(ComponentType type) {
        poolFlags |= type.flag();
    }
}
