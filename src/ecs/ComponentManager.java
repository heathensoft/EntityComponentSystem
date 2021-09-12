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


public class ComponentManager extends ECSManager{

    private MemoryManagerOld memoryManager;

    private final Map<Class<? extends Component>,ComponentType> typeMap;
    private final Container<ComponentType> typesById;
    private long nextFlag = 1L;
    private byte genTypeID = 0;

    private final Container<ComponentGroup> groups;
    private short genGroupID = 0;

    private final Container<Container<Component>> components;

    private final Container<ComponentPool<? extends Component>> pools;
    private long poolFlags = 0L;

    private int componentCount = 0;         // components in play
    private long componentsAdded = 0L;      // total number of components added
    private long componentsRemoved = 0L;    // total number of components removed
    private long componentsDiscarded = 0L;  // total number of components removed and NOT returned to a pool

    protected ComponentManager() {
        typeMap = new HashMap<>();
        groups = new Container<>();
        components = new Container<>(9); // 9 will eventually hit 64 (Max) on resizing
        typesById = new Container<>(9);
        pools = new Container<>(9);
    }

    @Override
    protected void set(ECS ecs) {
        this.memoryManager = ecs.memoryManager;
    }

    @Override
    protected void terminate() {

        pools.iterate(pool -> pool.clear(false));
        pools.clear();
        components.iterate(Container::clear);
        components.clear();
        groups.clear();
        typeMap.clear();


    }

    protected  <T extends Component> void registerPool(ComponentPool<T> pool, Class<T> clazz) {
        ComponentType type = getType(clazz);
        if (poolRegistered(type))
            throw new IllegalStateException("Duplicate pool");
        occupyPoolSlot(type);
        pool.register(memoryManager,type);
        pools.set(pool,type.id);
    }

    /**
     * Any replaced component get pooled if pool is registered for type.
     *
     * @param e the entity
     * @param c the component to be added to entity
     * @return whether another component of same type as c was replaced by c
     */
    protected boolean addComponent(Entity e, Component c) {
        if (c == null) throw new IllegalStateException("Component cannot be null");
        boolean replaced;
        ComponentType type = getType(c.getClass());
        if (e.hasComponent(type.flag)) {
            Component removed = removeComponentFromContainer(e.id,type.id);
            if (removed == null) throw new IllegalStateException("Component should not be null atp");
            if (poolRegistered(type)) // if pool is registered and receives components of c's type;
                pools.get(type.id).freeInternal(c);
            replaced = true;
        } else {
            replaced = false;
            e.addComponent(type.flag);
            memoryManager.resetContainerTimer(type.id);
            componentCount++;
        }
        components.get(type.id).set(c,e.id);
        return replaced;
    }

    protected void removeAll(Entity e) {
        Container<Component> componentsByType;
        Component component;
        e.setComponents(0);
        for (int i = 0; i < uniqueTypes(); i++) {
            componentsByType = components.get(i);
            if (e.id < componentsByType.usedSpace()) {
                component = componentsByType.remove(e.id);
                if (component != null) {
                    componentCount--;
                    ComponentType type = typesById.get(i);
                    memoryManager.resetContainerTimer(type.id);
                    if (poolRegistered(type))
                        pools.get(type.id).freeInternal(component);
                }
            }
        }
    }

    protected boolean removeComponent(Entity e, ComponentType t) {
        if (!e.hasComponent(t.flag)) return false;
        Component c = removeComponentFromContainer(e.id,t.id);
        if (c == null)
            throw new IllegalStateException("Component should not be null atp");
        e.removeComponent(t.flag);
        if (poolRegistered(t))
            pools.get(t.id).freeInternal(c);
        else
        memoryManager.resetContainerTimer(t.id);
        componentCount--;
        return true;
    }

    /**
     * Removes a Component of the same type as c from the entity.
     * What matters is the type of c. It does not check for equals.
     * It could remove another component entirely (of the same type) than the passed in c.
     *
     * Removed components get pooled if pool registered for type.
     *
     * @param e the entity
     * @param c the component to be removed (Only the type of c matters)
     * @return whether a component was removed
     */
    protected boolean removeComponent(Entity e, Component c) {
        return removeComponent(e,getType(c.getClass()));
    }

    private Component removeComponentFromContainer(int entityID, byte typeID) {
        Container<Component> container = components.get(typeID);
        if (entityID < container.usedSpace())
            return container.remove(entityID);
        return null;
    }

    /**
     * Unsafe. Does not check for index outOfBounds.
     * see: ecs.Getter.java. The only class that use this.
     * public equivalent: getComponent()
     *
     * @param e the entity
     * @param t the type
     * @return the component
     */
    protected Component getComponentUnsafe(Entity e, ComponentType t) {
        return components.get(t.id).get(e.id);
    }

    public Component getComponent(Entity e, ComponentType t) {
        Container<Component> container = components.get(t.id);
        if (e.id < container.usedSpace())
            return container.get(e.id);
        return null;
    }

    public ComponentType getType(Class<? extends Component> c) {
        ComponentType type = typeMap.get(c);
        if (type == null) {
            if (uniqueTypes() == Long.SIZE)
                throw new IllegalStateException("limit break: maximum unique component-types");
            type = new ComponentType(c,nextFlag,genTypeID);
            nextFlag = nextFlag << 1;
            genTypeID++;
            typeMap.put(c,type);
            typesById.push(type);
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
        return typeMap.size();
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

    public boolean poolRegistered(ComponentType type) {
        return (poolFlags & type.flag) == type.flag;
    }

    private void occupyPoolSlot(ComponentType type) {
        poolFlags |= type.flag;
    }
}
