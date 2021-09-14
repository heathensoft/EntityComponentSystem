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

    protected final RunTimeStatistics runTimeStatistics;
    protected final ComponentManager componentManager;
    protected final SystemManager systemManager;
    protected final EntityManager entityManager;

    private Diagnostics diagnostics;
    private boolean initialized;

    public ECS(int initialCapacity, int maxPoolSize) {

        runTimeStatistics = new RunTimeStatistics(this);
        componentManager = new ComponentManager(this);
        entityManager = new EntityManager(this,initialCapacity,maxPoolSize);
        systemManager = new SystemManager(this);

    }

    public void initialize() {
        if (!initialized) {
            systemManager.initializeSystems();
            initialized = true;
        }
    }

    public void update(float dt) {


    }

    public synchronized void runDiagnostics(Diagnostics diagnostics) {
        if (diagnostics == null)
            throw new IllegalArgumentException("null argument");
        if (this.diagnostics != null) {
            try {
                this.diagnostics.awaitTermination();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        this.diagnostics = diagnostics;
        diagnostics.set(runTimeStatistics);
        new Thread(diagnostics).start();
    }

    public void registerSystem(EntitySystem system) {
        if (initialized) throw new IllegalStateException("Register systems before ECS initialization");
        systemManager.register(system);
    }

    public <T extends Component> void registerComponentPool(ComponentPool<T> pool, Class<T> clazz) {
        if (!initialized) throw new IllegalStateException("Register pools before ECS initialization");
        componentManager.registerPool(pool,clazz);
    }

    public <T extends EntitySystem> T getSystem(Class<T> systemClass) {
        return systemManager.getSystem(systemClass);
    }

    public ComponentType getType(Class<? extends Component> componentClass) {
        return componentManager.getType(componentClass);
    }

    public ComponentGroup getGroup(ComponentType... componentTypes) {
        return componentManager.getGroup(componentTypes);
    }

    @SafeVarargs
    public final ComponentGroup getGroup(Class<? extends Component>... classes) {
        return componentManager.getGroup(classes);
    }

    public <T extends Component> Getter<T> newGetter(Class<T> componentClass) {
        return new Getter<>(componentClass,componentManager);
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

        systemManager.terminate();
        entityManager.terminate();

        componentManager.terminate();
        System.gc(); // The only place the ECS invokes gc()
    }



    public RunTimeStatistics runTimeStatistics() {
        return runTimeStatistics;
    }

    public ComponentManager componentManager() {
        return componentManager;
    }

    public EntityManager entityManager() {
        return entityManager;
    }

    public SystemManager systemManager() {
        return systemManager;
    }


    protected boolean isInitialized() {
        return initialized;
    }


}
