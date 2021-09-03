package ecs;

import ecs.util.Pool;

/**
 * @author Frederik Dahl
 * 02/09/2021
 */


public abstract class ComponentPool<T extends Component> extends Pool<T> {

    protected ComponentManager componentManager;
    protected ComponentType componentType;

    public ComponentPool(int initialCap, int max) {
        super(initialCap, max);
    }

    protected void initialize(ComponentManager componentManager, ComponentType componentType) {
        this.componentManager = componentManager;
        this.componentType = componentType;
    }

    @Override
    protected void discard(T object) {
        componentManager.discardCount++;
    }
}
