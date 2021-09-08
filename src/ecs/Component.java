package ecs;


/**
 *
 * Empty slate. Marker-interface
 *
 * A component is ideally just a struct of data. EntitySystems should provide all the functionality.
 * When linked to an Entity, EntitySystems that are interested will add the entity to itself.
 *
 *
 * Use of ComponentPool:
 * To register a pool in the system: ecs.componentManager.addPool(thePool,clazz);
 * The system will then be able to return components back into the pool, and it can be
 * monitored by the MemoryManager.
 *
 * ComponentPools and data reset:
 * You can reset the Components' data internally by having it implement the Poolable Interface,
 * Or you could let the ComponentPool handle it ( resetComponent() ).
 * Both methods are called when the component instance is returned to the pool (not discarded).
 *
 *
 * @author Frederik Dahl
 * 20/08/2021
 */


public interface Component {



}
