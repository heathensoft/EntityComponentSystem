package com.nudge.ecs;

/**
 *
 * The top class of the Entity component system.
 *
 * Use the EntityManager to create entities and give them components.
 * Use the RunTimeStatistics to query things like active entities count etc.
 * You can run diagnostics to (though it's mostly for debugging while im developing)
 * If you want the system to manage capacity for component-pools and containers
 * that has been inactive for more than 5 min, i.e. shrink them if possible,
 * you need to run capacity control anywhere in your game-loop.
 *
 * But before you can use the ECS you need to initialize it:
 *
 * The initialization order:
 *
 * 1. Create a new ECS
 * 2. Create the Systems and register pools
 * 3. Initialize the ECS
 * 4. Good to go
 *
 * @author Frederik Dahl
 * 01/09/2021
 */


public class ECS {

    protected final RunTimeStatistics runTimeStatistics;
    protected final ComponentManager componentManager;
    protected final SystemManager systemManager;
    protected final EntityManager entityManager;

    private Diagnostics diagnostics;
    private boolean initialized;

    public ECS(int initialCap) {
        runTimeStatistics = new RunTimeStatistics(this);
        componentManager = new ComponentManager(this);
        entityManager = new EntityManager(this,initialCap);
        systemManager = new SystemManager(this);
    }

    public void initialize() {
        if (!initialized) {
            systemManager.initializeSystems();
            initialized = true;
        }
    }

    public void capacityControl(float dt) {
        componentManager.control.check(dt);
    }


    public <T extends Component> void registerComponentPool(ComponentPool<T> pool, Class<T> clazz) {
        if (!initialized) throw new IllegalStateException("Register pools before ECS initialization");
        componentManager.registerPool(pool,clazz);
    }

    public <T extends ECSystem> T getSystem(Class<T> systemClass) {
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

    public <T extends Component> Getter<T> getter(Class<T> componentClass) {
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
     * 3. Deletes all entities from the EntityManager. Clearing the pool.
     * 4. Clears all component pools and containers.
     * 5. Terminating Diagnostics if running. Waiting for it to finish.
     * 6. Nullifying component pools and containers.
     * 6. Garbage Collection is triggered
     *
     */
    public void terminate() {
        systemManager.deactivateSystems();
        // stop and join system threads if any
        entityManager.terminate();
        componentManager.clearContainers();
        systemManager.terminate();
        stopDiagnostics();
        componentManager.nullify();
        System.gc();
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



    protected boolean isInitialized() {
        return initialized;
    }

    public synchronized void setDiagnostics(Diagnostics diagnostics) {
        if (diagnostics == null)
            throw new IllegalArgumentException("null argument");
        stopDiagnostics();
        this.diagnostics = diagnostics;
        diagnostics.set(runTimeStatistics);
    }

    public synchronized void runDiagnostics() {
        if (diagnostics != null) {
            terminateDiagnostics();
            new Thread(diagnostics).start();
        }
    }

    public synchronized void stopDiagnostics() {
        if (diagnostics != null) {
            terminateDiagnostics();
        }
    }

    private void terminateDiagnostics() {
        try {
            diagnostics.awaitStop();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


}
