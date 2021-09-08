package ecs;


import ecs.util.KVArray;

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

    protected final void revalidate(Entity e) {
        final boolean inSystem = e.inSystem(systemBit);
        final boolean hasComponents = group.containsAll(e.components);
        if (e.enabled) {
            if (inSystem && hasComponents) return;
            if (!inSystem && hasComponents) addEntity(e);
            else if (inSystem) removeEntity(e); // !hasComponents == true atp
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
