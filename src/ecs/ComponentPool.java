package ecs;

import ecs.util.Pool;

/**
 * ComponentPool
 *
 * Component pools must be registered by the ECS: ecs.componentManager.addPool(pool);
 * Before the ESC is initialized: ecs.initialize();
 *
 * You don't need to use Pools, but there are benefits of extending (and registering) a ComponentPool:
 *
 * ECS returns removed components back to registered pool automatically.
 *
 * Components can implement Poolable.
 *
 * It will create new instances only if they aren't available in the pool. Pools can also be pre-filled.
 *
 * It lets the MemoryManager keep track of various component/pool life-cycle statistics (available in ecs.memoryManager)
 * Stats for any specific pool are available in the that pool. Global (total) pool stats are stored in the MemoryManager.
 *
 * The MemoryManager will also query inactive pools (pools unused for specifically 5 min) to check if it's possible
 * to resize its storage space. It will NOT shrink anything below the InitialCapacity.
 * That is useful if a huge amount (any amount over initial cap) of components were created, but perhaps that component type
 * isn't used very often. It will then check if it's possible to shrink the capacity back towards initialCapacity.
 *
 * @author Frederik Dahl
 * 02/09/2021
 */

public abstract class ComponentPool<T extends Component> extends Pool<T> {

    protected MemoryManager memoryManager;
    protected ComponentType componentType;

    private int totalDiscarded = 0;
    private int totalObtained = 0;
    private int totalRecycled = 0;

    public ComponentPool(int initialCapacity, int maxCapacity) {
        super(initialCapacity, maxCapacity);
    }

    protected void register(MemoryManager memoryManager, ComponentType componentType) {
        this.memoryManager = memoryManager;
        this.componentType = componentType;
    }

    public ComponentType componentType() {
        return componentType;
    }

    public int componentsCreated() {
        return totalObtained;
    }

    public int componentsDestroyed() {
        return totalDiscarded;
    }

    public int componentsRecycled() {
        return totalRecycled;
    }

    public int componentsInMemory() {
        return newInstancesCreated() - totalDiscarded;
    }

    @SuppressWarnings("unchecked")
    protected void freeInternal(Component c) {
        free((T)c);
    }

    @Override
    protected void onObjectPooled(T object) {
        memoryManager.resetPoolTimer(componentType.id);
        totalRecycled++;
    }

    @Override
    protected void onObjectDiscarded(T object) {
        totalDiscarded++;
    }

    @Override
    protected void onObjectObtained(T object, boolean newInstance) {
        if (!newInstance)
            memoryManager.resetPoolTimer(componentType.id);
        totalObtained++;
    }
}
