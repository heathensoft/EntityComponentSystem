package ecs;


import ecs.util.containers.KVArray;

/**
 *
 * @author Frederik Dahl
 * 20/08/2021
 */


public abstract class EntitySystem {
    

    protected EntityManager entityManager;
    protected final KVArray<Entity> entities;
    protected final ComponentGroup group;
    protected long systemBit;
    protected boolean activated;


    public EntitySystem(ComponentGroup group, int initialCapacity) {
        entities = new KVArray<>(initialCapacity);
        this.group = group;
    }

    protected final void set(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    /**
     * Any state-changes to an entity (add/rem-components, enable/disable)
     * will in most cases trigger its revalidation by each registered system.
     * Systems will appropriately keep, add or remove the entity.
     *
     * @param e the entity e to be revalidated by the system
     */

    // this setup should have the least possible operations. Using positive operators only :)
    // 1.   if the entity is disabled, we only need to check if it is in the system. If it is, remove it.
    // 2.   in the case enabled == true, we first check the status-quo to see if we can return immediately.
    //      status-quo being: its both in the system and has the required components. OR the opposite.
    //      That can be simplified to: inSystem == hasComponents. (No change)
    // 3.   now we only need to know if it's in the system. if true, we know its missing the components,
    //      and therefore we remove it. else we know it meets the requirements, and we add it.

    protected final void revalidate(Entity e) {
        final boolean inSystem = e.inSystem(systemBit);
        if (e.isEnabled()) {
            if (inSystem == group.containsAll(e.components())) return;
            if (inSystem) removeEntity(e);
            else addEntity(e);
        } else if (inSystem) removeEntity(e);
    }

    private void addEntity(Entity e) {
        e.addSystem(systemBit);
        entities.add(e);
    }

    private void removeEntity(Entity e) {
        e.removeSystem(systemBit);
        entities.remove(e); // O(1) removal
    }
    

    protected void initialize() {

    }

    public final void update() {
        if (activated) {
            begin();
            process();
            entityManager.clean();
            end();
        }
    }

    /**
     * This is called on SystemManager Termination.
     * All entities in the system has already been removed atp.
     * There is no need to clear the entities.
     */
    protected void terminate() {

    }

    protected void begin() {

    }

    protected abstract void process();


    protected void end() {

    }
    
    public void activate() {
        activated = true;
    }

    public void deactivate() {
        activated = false;
    }

    protected void setSystemBit(long bit) {
        this.systemBit = bit;
    }

    protected long getSystemBit() {
        return systemBit;
    }

    public ComponentGroup getGroup() {
        return group;
    }

}
