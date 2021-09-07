package ecs;


import ecs.util.KVArray;

/**
 *
 * @author Frederik Dahl
 * 20/08/2021
 */


public abstract class EntitySystem {
    

    private EntityManager entityManager;
    private final KVArray<Entity> entities;
    private final ComponentGroup group;
    private long systemBit;
    private boolean activated;


    public EntitySystem(ComponentGroup group, int initialCapacity) {
        entities = new KVArray<>(initialCapacity);
        this.group = group;

    }

    protected void set(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    protected void revalidate(Entity e) {

        boolean contains = entities.contains(e); // this should be fast. could replace with
        boolean hasComponents = group.containsAll(e.components);
        boolean enabled = e.enabled;

        if (contains && hasComponents) {
            if (enabled) return;
            removeEntity(e);
        }


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

    private void addEntity(Entity e) {

    }

    private void removeEntity(Entity e) {

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
