package ecs;

/**
 *
 * This is the "hub" for ECS statistics. Contains getters only.
 * Query stats directly or in separate thread, using Diagnostics.
 * Running diagnostics is the purpose of this class.
 * It's thread safe, but may be inaccurate by a couple of frames.
 * No methods here should interrupt or cause issues in the ECS main-thread.
 *
 * @author Frederik Dahl
 * 09/09/2021
 */

// bør også ha deltaTime.

public class RunTimeStatistics {


    private final ECS ecs;

    public RunTimeStatistics(ECS ecs) {
        this.ecs = ecs;
    }


    // Entities

    public int entitiesActive() {return ecs.entityManager.entities(); }

    public long entitiesCreated() { return ecs.entityManager.entitiesCreated(); }

    public long entitiesLost() { return ecs.entityManager.entitiesDestroyed(); }

    public int entitiesInMemory() { return ecs.entityManager.entitiesInMemory(); }


    // Components (pools and containers)

    public int componentsActive() { return ecs.componentManager.componentsActive(); }

    public int componentsPooled() { return ecs.componentManager.pools.inPoolTotal(); }

    public int componentsPooled(ComponentType type) { return ecs.componentManager.pools.inPool(type); }

    public long componentsObtained() { return ecs.componentManager.pools.obtainedTotal(); }

    public long componentsObtained(ComponentType type) { return ecs.componentManager.pools.obtained(type); }

    public float componentPoolsLoadFactor() { return ecs.componentManager.pools.loadFactorAll(); }

    public float componentPoolLoadFactor(ComponentType type) { return ecs.componentManager.pools.loadFactor(type); }

    public float componentContainersLoadFactor() { return ecs.componentManager.getContainersLoadFactor(); }

    public float componentContainerLoadFactor(ComponentType type) { return ecs.componentManager.getContainerLoadFactor(type); }

    public long componentsAdded() { return ecs.componentManager.componentsAdded(); }

    public long componentsRemoved() { return ecs.componentManager.componentsRemoved(); }

    public long componentsDiscarded() { return ecs.componentManager.pools.discardedTotal(); }

    public long componentsDiscarded(ComponentType type) { return ecs.componentManager.pools.discarded(type); }

    public long componentsLost() { return ecs.componentManager.componentsLost(); }

    public int componentsInMemory() { return componentsActive() + componentsPooled(); }

    public int componentPools() { return ecs.componentManager.pools.poolCount(); }

    public int poolRefits() { return ecs.componentManager.poolRefits(); }

    public int containerRefits() { return ecs.componentManager.containerRefits(); }





    private int memoryUsageMB() {
        Runtime runtime = Runtime.getRuntime();
        int free = (int)(runtime.freeMemory()/1000000L);
        int total = (int)(runtime.totalMemory()/1000000L);
        return total - free;
    }

    private float memoryUsagePercent() {
        Runtime runtime = Runtime.getRuntime();
        int free = (int)(runtime.freeMemory()/1000000L);
        int total = (int)(runtime.totalMemory()/1000000L);
        return (float) (Math.round((1 - (float)free / total) * 10000) / 100.0);
    }

}
