package ecs;

/**
 * @author Frederik Dahl
 * 09/09/2021
 */

// bør også ha deltaTime.

public class RunTimeStatistics {


    private final ECS ecs;

    public RunTimeStatistics(ECS ecs) {
        this.ecs = ecs;
    }


    // add synchronized to methods looping / querying anything. Or at least think it through. where should the sync be etc.

    public int entitiesActive() { return ecs.entityManager.entities(); }

    public long entitiesCreated() { return ecs.entityManager.entitiesCreated(); }

    public long entitiesLost() { return ecs.entityManager.entitiesDestroyed(); }

    public int entitiesInMemory() { return ecs.entityManager.entitiesInMemory(); }


    public int componentsActive() { return ecs.componentManager.componentsActive(); }

    public int componentsPooled() { return ecs.componentManager.pools.inPoolTotal(); }

    public int componentsPooled(ComponentType type) { return ecs.componentManager.pools.inPool(type); }

    public long componentsObtained() { return 0; }

    public int componentsObtained(ComponentType type) { return 0; }

    public float componentPoolsLoadFactor() { return 0; }

    public float componentPoolLoadFactor(ComponentType type) { return 0; }

    public float componentContainersLoadFactor() { return 0; }

    public float componentContainerLoadFactor(ComponentType type) { return 0; }

    public long componentsAdded() { return ecs.componentManager.componentsAdded(); }

    public long componentsRemoved() { return ecs.componentManager.componentsRemoved(); }

    public long componentsDiscarded() { return 0; }

    public long componentsDiscarded(ComponentType type) { return ecs.componentManager.pools.discarded(type); }

    public long componentsLost() { return 0; }

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
