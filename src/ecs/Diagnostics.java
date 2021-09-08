package ecs;

/**
 *
 * Debugging class
 *
 * Used to store ECS statistics, logging and output .csv
 *
 * @author Frederik Dahl
 * 08/09/2021
 */


public class Diagnostics {

    // should have a load-factor stat

    int componentsInPlay = 0;                       // components currently on entities
    int totalComponentsInPools = 0;                 // components stored in pools
    long totalComponentsObtainedFromPools = 0L;     // components obtained from pools
    long totalComponentsAdded = 0L;                 // components added to entities
    long totalComponentsRemoved = 0L;               // components removed from entities
    long totalComponentsLost = 0L;                  // components removed and lost reference
    long totalComponentsDiscarded = 0L;             // components discarded from pools

    int componentContainerRefits = 0;
    int componentPoolRefits = 0;


    public int componentsInMemory() {
        return totalComponentsInPools + componentsInPlay;
    }
}
