package ecs;

/**
 * A" component getter" helper class for fast queries of a componentManger's component containers.
 *
 * @author Frederik Dahl
 * 05/09/2021
 */


public class Getter<T extends Component>{

    private final Class<T> clazz;
    private final ComponentType componentType;
    private final ComponentManager componentManager;

    public Getter(Class<T> clazz, ComponentManager componentManager) {
        this.componentType = componentManager.getType(clazz);
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
        return (T)componentManager.getComponentUnsafe(e,componentType);
    }
    /**
     * Class-cast with checking for index out of bounds
     *
     * @param e the entity
     * @return the component cast to: (T extends Component), or null
     */
    public final T get(Entity e) {
        return clazz.cast(componentManager.getComponent(e,componentType));
    }
}
