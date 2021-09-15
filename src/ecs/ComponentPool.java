package ecs;

import ecs.util.Pool;

/**
 * ComponentPool
 *
 * Component pools must be registered by the ECS: ecs.componentManager.addPool(pool);
 * before the ESC is initialized: ecs.initialize();
 *
 * You don't need to use Pools, but there are benefits of creating (and registering) a ComponentPool:
 *
 * The ECS returns removed components back to a registered pool automatically.
 *
 * Components can implement Poolable.
 *
 * It will create new instances only when the pool is empty.
 *
 * Pools can be pre-filled.
 *
 * It lets the MemoryManager keep track of various component/pool life-cycle statistics (available in ecs.memoryManager)
 * Stats for a specific pool are available in the pool itself. Global (total) pool stats are stored in the MemoryManager.
 *
 * The MemoryManager will also query inactive pools (pools unused for specifically 5 min) to check if it's possible
 * to resize its storage space. It will NOT shrink anything below it's InitialCapacity.
 * This is useful when a sufficient amount (any amount above its initial cap) of components were created
 * and removed again. It will then check whether it's possible to shrink the capacity back down towards initialCapacity.
 *
 *
 * @author Frederik Dahl
 * 02/09/2021
 */

public abstract class ComponentPool<T extends Component> extends Pool<T> {

    protected CapacityControl capacityControl;
    protected ComponentType componentType;

    public ComponentPool(int initialCapacity) {
        super(initialCapacity);
    }

    protected void register(CapacityControl capacityControl, ComponentType componentType) {
        this.capacityControl = capacityControl;
        this.componentType = componentType;
    }

    public ComponentType componentType() {
        return componentType;
    }

    @SuppressWarnings("unchecked")
    protected void freeInternal(Component c) {
        free((T)c);
    }

    @Override
    protected void reset(T c) {
        capacityControl.resetPoolTimer(componentType.id());
        resetComponent(c);
    }

    @Override
    protected void obtained(T c, boolean newInstance) {
        if (!newInstance) capacityControl.resetPoolTimer(componentType.id());
    }

    protected abstract void resetComponent(T c);
}
