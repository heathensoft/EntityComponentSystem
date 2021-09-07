package ecs;

import ecs.util.Container;
import ecs.util.Iterator;

/**
 * @author Frederik Dahl
 * 01/09/2021
 */


public class ECS {

    public ComponentManager componentManager; // protected final
    public SystemManager systemManager; // protected final
    public EntityManager entityManager; // protected final
    public MemoryManager memoryManager; // protected final

    private boolean initialized;

    public ECS() {

    }

    public void initialize() {

        initialized = true;
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



        System.gc();
    }
}
