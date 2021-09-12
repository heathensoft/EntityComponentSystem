package ecs;

/**
 * @author Frederik Dahl
 * 09/09/2021
 */


public class RunTimeStatistics {

    int componentsInPlay = 0;                  // components currently on entities
    int componentsInPools = 0;                 // components stored in pools
    long componentsObtainedFromPools = 0L;     // components obtained from pools
    long componentsAdded = 0L;                 // components added to entities
    long componentsRemoved = 0L;               // components removed from entities
    long componentsLost = 0L;                  // components removed and lost reference. set in comp manager
    long componentsDiscarded = 0L;             // components discarded from pools

    int entitiesInPlay = 0;
    int entitiesInPool = 0;
    long entitiesCreated = 0L;
    long entitiesLost = 0L;


    int componentContainerRefits = 0;
    int componentPoolRefits = 0;

    int memoryUsageMB = 0;
    float memoryUsagePercent = 0f;



    protected void intervalUpdate(long[] newTotals) {

    }

    public int getComponentsInPlay() {
        return componentsInPlay;
    }

    public int getComponentsInPools() {
        return componentsInPools;
    }

    public long getComponentsObtainedFromPools() {
        return componentsObtainedFromPools;
    }

    public long getComponentsAdded() {
        return componentsAdded;
    }

    public long getComponentsRemoved() {
        return componentsRemoved;
    }

    public long getComponentsLost() {
        return componentsLost;
    }

    public long getComponentsDiscarded() {
        return componentsDiscarded;
    }

    public int getEntitiesInPlay() {
        return entitiesInPlay;
    }

    public int getEntitiesInPool() {
        return entitiesInPool;
    }

    public long getEntitiesCreated() {
        return entitiesCreated;
    }

    public long getEntitiesLost() {
        return entitiesLost;
    }

    public int getComponentContainerRefits() {
        return componentContainerRefits;
    }

    public int getComponentPoolRefits() {
        return componentPoolRefits;
    }

    public int getComponentsInMemory() {
        return componentsInPools + componentsInPlay;
    }

    public int getEntitiesInMemory() {
        return entitiesInPool + entitiesInPlay;
    }
}
