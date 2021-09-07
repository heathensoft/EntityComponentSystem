package ecs;


import ecs.util.Container;
import ecs.util.Iterator;

import java.util.Arrays;

/**
 *
 * The MemoryManager is not strictly necessary for most use-cases.
 * But it is useful with a large amount of entities and components being
 * created and destroyed in a long-running application.
 * It keeps track of parts of the system that's been inactive for a while (5 min)
 * then tries to minimize the memory footprint of pools and containers.
 * It won't call the garbage collector explicitly.
 *
 * It also keeps track of run-time ECS statistics for debugging purposes.
 * It runs in 20 seconds time-step. Some component stats have a 20sec delay.
 *
 * Timers are added automatically when a new ComponentType has been created by the ComponentManager.
 * Specific container timers are started / reset when a new component of given type are added to
 * the ComponentManager. If a container has been inactive for 5 minutes. It will check if
 * it's possible to resize the container. That happens if the capacity > target capacity.
 * It will never shrink containers below that target.
 * After a check, the timer will pause until triggered again.
 * In addition to component containers, if you add pools to the system. The MemoryManager will
 * query the pools in the same manner.
 *
 *
 * There are two timers for each index of the short[]:
 *
 * LSB - Containers (4bit - 15 time-steps of 20 seconds)
 * MSB - Pools      (4bit - 15 time-steps of 20 seconds)
 *
 * The index is the ComponentTypes' id.
 *
 *
 * @author Frederik Dahl
 * 02/09/2021
 */


public class MemoryManager {

    private static final short TIME_STEP            = 0x14;
    private static final short CONTAINER_CHECKED    = 0x0F;
    private static final short POOL_CHECKED         = 0xF0;
    private static final short UP_TO_DATE           = 0xFF;

    private final short[] timers;
    private float accumulator;
    private byte timerCount;
    private boolean enabled;
    private int componentsInMemory;
    private long componentsCreated;
    private long componentsDestroyed;
    private float memoryUsage;
    private int memoryUsageMB;
    private int refitCount;

    private final Iterator<ComponentPool<? extends Component>> poolIterator = new Iterator<>() {
        @Override
        public void next(ComponentPool<? extends Component> pool) {
            componentsDestroyed += pool.discarded();
            componentsInMemory += pool.objectsInMemory();
            componentsCreated += pool.obtained();
        }
    };

    private final EntityManager entityManager;
    private final ComponentManager componentManager;

    protected MemoryManager(ECS ecs) {
        if (ecs == null) throw new IllegalStateException("");
        componentManager = ecs.componentManager;
        entityManager = ecs.entityManager;
        timers = new short[Long.SIZE];
        Arrays.fill(timers,UP_TO_DATE);
        componentsInMemory = 0;
        componentsCreated = 0;
        componentsDestroyed = 0;
        timerCount = 0;
        accumulator = 0;
        refitCount = 0;
        enabled = true;
        queryRuntimeMemory();
    }

    protected void update(float dt) {

        accumulator += dt;
        // 20 second time-interval
        if (accumulator > TIME_STEP) {
            accumulator -= TIME_STEP;
            queryRuntimeMemory();

            // Update component statistics from pool info. // do this and sys-out on termination
            if (poolCount() != 0) {
                Container<ComponentPool<? extends Component>> pools;
                pools = componentManager.getPools();
                resetComponentStats();
                pools.iterate(poolIterator);
            }

            // check inactive component pools and containers
            if (enabled) {
                for (byte i = 0; i < timerCount; i++) {
                    if (timers[i] != UP_TO_DATE) {
                        if (!containerTimerMaxed(timers[i])) {
                            if (containerTimerMaxed(++timers[i])) {
                                if (componentManager.autoFitContainer(i))
                                    refitCount++;
                            }
                        }
                        if (!poolTimerMaxed(timers[i])) {
                            if (poolTimerMaxed(timers[i] += 0x10)) {
                                if (componentManager.autoFitPool(i))
                                    refitCount++;
                            }
                        }
                    }
                }
            }
        }
    }

    protected void newTimer() {
        timerCount++;
    }

    protected void resetContainerTimer(byte index) {
        timers[index] &= ~CONTAINER_CHECKED;
    }

    protected void resetPoolTimer(byte index) {
        timers[index] &= ~POOL_CHECKED;
    }

    private boolean containerTimerMaxed(short timer) {
        return (timer & CONTAINER_CHECKED) == CONTAINER_CHECKED;
    }

    private boolean poolTimerMaxed(short timer) {
        return (timer & POOL_CHECKED) == POOL_CHECKED;
    }

    private void queryRuntimeMemory() {
        Runtime runtime = Runtime.getRuntime();
        int free = (int)(runtime.freeMemory()/1000000L);
        int total = (int)(runtime.totalMemory()/1000000L);
        memoryUsage = (float) (Math.round((1 - (float)free / total) * 10000) / 100.0);
        memoryUsageMB = total - free;
    }

    private void resetComponentStats() {
        componentsCreated = 0;
        componentsInMemory = 0;
        componentsDestroyed = 0;
    }

    public void enable() {
        enabled = true;
    }

    public void disable() {
        enabled = false;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public float getMemoryUsage() {
        return memoryUsage;
    }

    public int getMemoryUsageMB() {
        return memoryUsageMB;
    }

    public int entitiesInPlay() {
        return entityManager.entities();
    }

    public long entitiesCreated() {
        return entityManager.entitiesCreated();
    }

    public int entitiesDestroyed() {
        return entityManager.entitiesDestroyed();
    }

    public int entitiesInMemory() {
        return entityManager.entitiesInMemory();
    }

    public int componentInPlay() {
        return componentManager.componentCount();
    }

    public long componentsCreated() {
        return componentsCreated;
    } // components obtained

    public long componentsDestroyed() {
        return componentsDestroyed;
    }

    public int componentsInMemory() {
        return componentsInMemory;
    }

    public int containerRefits() {
        return refitCount;
    }

    public int poolCount() {
        return componentManager.poolCount();
    }


}
