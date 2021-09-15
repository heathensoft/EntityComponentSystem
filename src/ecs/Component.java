package ecs;


/**
 *
 * Empty slate. Marker-interface
 *
 * A component is ideally just a struct of data. EntitySystems should provide all the functionality.
 * When a component get added to an Entity, EntitySystems that are interested will absorb the entity to itself.
 * When a component get removed from an entity, EntitySystems no longer interested will purge it.
 *
 * Use of ComponentPool:
 * To register a pool in the system: ecs.registerPool(thePool,clazz);
 * The system will then be able to return components back into the pool, and it can be
 * monitored by the ECS: capacity control, run-time statistics
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
