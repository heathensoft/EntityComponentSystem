package ecs;

/**
 * @author Frederik Dahl
 * 01/09/2021
 */


public class ECS {

    protected final ComponentManager componentManager;
    protected final SystemManager systemManager;
    protected final EntityManager entityManager;
    protected final MemoryManager memoryManager;

    private boolean initialized;

    public ECS(int initialCapacity, int maxPoolSize) {

        componentManager = new ComponentManager();
        entityManager = new EntityManager(initialCapacity,maxPoolSize);
        memoryManager = new MemoryManager();
        systemManager = new SystemManager();

        componentManager.set(this);
        entityManager.set(this);
        memoryManager.set(this);
        systemManager.set(this);
    }

    public void initialize() {
        if (!initialized) {
            systemManager.initialize();
            memoryManager.initialize();
            initialized = true;
        }
    }

    public void update(float dt) {

    }

    public void registerSystem(EntitySystem system) {
        if (initialized) throw new IllegalStateException("Register systems before ECS initialization");
        systemManager.register(system);
    }

    public <T extends Component> void registerComponentPool(ComponentPool<T> pool, Class<T> clazz) {
        if (!initialized) throw new IllegalStateException("Register pools before ECS initialization");
        componentManager.registerPool(pool,clazz);
    }

    public void terminate() {

        componentManager.terminate();
        systemManager.terminate();
        entityManager.terminate();
        memoryManager.terminate();

        System.gc();
    }

    public MemoryManager getMemoryManager() {
        return memoryManager;
    }

    public ComponentManager getComponentManager() {
        return componentManager;
    }

    public EntityManager getEntityManager() {
        return entityManager;
    }

    public SystemManager getSystemManager() {
        return systemManager;
    }
}
