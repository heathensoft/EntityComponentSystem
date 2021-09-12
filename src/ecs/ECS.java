package ecs;

/**
 *
 *
 * @author Frederik Dahl
 * 01/09/2021
 */


public class ECS {

    // todo:
    // Could eventually also manage threads for Systems. Some Runnable-provider Manager
    // Diagnostics has its own thread. I can see AI-Systems / pathfinding using concurrency

    protected final ComponentManager componentManager;
    protected final SystemManager systemManager;
    protected final EntityManager entityManager;
    protected final MemoryManagerOld memoryManager;

    private boolean initialized;

    public ECS(int initialCapacity, int maxPoolSize) {

        componentManager = new ComponentManager();
        entityManager = new EntityManager(initialCapacity,maxPoolSize);
        memoryManager = new MemoryManagerOld();
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
        memoryManager.update(dt);
    }

    public void registerSystem(EntitySystem system) {
        if (initialized) throw new IllegalStateException("Register systems before ECS initialization");
        systemManager.register(system);
    }

    public <T extends Component> void registerComponentPool(ComponentPool<T> pool, Class<T> clazz) {
        if (!initialized) throw new IllegalStateException("Register pools before ECS initialization");
        componentManager.registerPool(pool,clazz);
    }

    /**
     * Terminates the ECS. Do not terminate directly from inside an EntitySystem update().
     * If necessary, use a flag. i.e. shouldTerminate = true.
     *
     * Termination is done systematically, followed by a call to gc().
     *
     * 1. Removes all components from entities.
     * 2. Cleaning / revalidating all entities, removing them from all systems.
     * 3. Deletes all entities from the EntityManager.
     * 4. MemoryManager prints lifecycle stats.
     * 5. Terminates all managers.
     *      Clears pools and containers.
     *      Nullifying of references.
     * 6. Garbage Collection
     *
     */
    public void terminate() {

        memoryManager.printStatistics();
        systemManager.terminate();
        entityManager.terminate();

        componentManager.terminate();
        memoryManager.terminate();
        memoryManager.printStatistics();
        System.gc(); // The only place the ECS invokes gc()
    }

    public MemoryManagerOld getMemoryManager() {
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
