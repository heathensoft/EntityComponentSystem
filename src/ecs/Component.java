package ecs;


/**
 *
 * Empty slate. Marker-interface
 *
 * It's supposed to be a functionless struct of public data.
 * When added to an Entity, EntitySystems that are interested will add the entity
 * to itself. The EntitySystem is where all the specific functionality goes.
 *
 * Create new instances or perhaps use ComponentPools for heavier ones:
 * To register a pool in the system: ecs.componentManager.addPool(thePool,clazz);
 * The system will then start to return components to the pool, and it can be
 * monitored by the MemoryManager.
 *
 *
 * @author Frederik Dahl
 * 20/08/2021
 */


public interface Component {



}
