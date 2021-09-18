package com.nudge.ecs;

/**
 * A" component getter" helper class for fast queries of ECS component containers.
 * You can create them in the ESC class
 *
 *
 * @author Frederik Dahl
 * 05/09/2021
 */


public class Getter<T extends Component>{

    private final byte typeID;
    private final Class<T> clazz;
    private final ComponentManager componentManager;

    /**
     * Available through the ECS instance getter(class); method
     * @param clazz the class
     * @param componentManager componentManager
     */
    protected Getter(Class<T> clazz, ComponentManager componentManager) {
        this.typeID = componentManager.getType(clazz).id();
        this.componentManager = componentManager;
        this.clazz = clazz;
    }
    /**
     * Direct casting without checking for index out of bounds.
     *
     * @param e the entity
     * @return the component cast to: (T extends Component), or null
     */
    @SuppressWarnings("unchecked")
    public final T getUnsafe(Entity e) {
        return (T)componentManager.getComponentUnsafe(e.id(),typeID);
    }
    /**
     * Class-cast with checking for index out of bounds
     *
     * @param e the entity
     * @return the component cast to: (T extends Component), or null
     */
    public final T get(Entity e) {
        return clazz.cast(componentManager.getComponent(e.id(),typeID));
    }
}
