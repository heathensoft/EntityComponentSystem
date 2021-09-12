package ecs;

import ecs.util.Container;
import ecs.util.Iterator;

import java.util.Arrays;

/**
 * @author Frederik Dahl
 * 09/09/2021
 */


public class Memory {

    private static final short INTERVAL = 0x14;

    protected RunTimeStatistics runTimeStatistics;
    protected ContainerControl containerControl;
    private ComponentManager componentManager;
    private EntityManager entityManager;

    boolean containerControlEnabled;
    float accumulator = 0.0f;
    long[] queryArray;

    private final Iterator<ComponentPool<? extends Component>> poolItr = new Iterator<>() {
        @Override
        public void next(ComponentPool<? extends Component> pool) {
            pool.query(queryArray);
        }
    };

    private void update(float dt) {
        accumulator += dt;
        if (accumulator > INTERVAL) {
            accumulator -= INTERVAL;
            containerControl.check(dt);
            updateRunTimeStatistics();
        }
    }

    private void updateRunTimeStatistics() {
        queryPools();
        queryRuntimeMemory();
    }

    private void queryPools() {
        Container<ComponentPool<? extends Component>> pools;
        pools = componentManager.getPools();
        Arrays.fill(queryArray,0L);
        pools.iterate(poolItr);

    }

    private void queryRuntimeMemory() {
        Runtime runtime = Runtime.getRuntime();
        int free = (int)(runtime.freeMemory()/1000000L);
        int total = (int)(runtime.totalMemory()/1000000L);
        runTimeStatistics.memoryUsagePercent = (float) (Math.round((1 - (float)free / total) * 10000) / 100.0);
        runTimeStatistics.memoryUsageMB = total - free;
    }

    public void toggleContainerControl(boolean on) {
        containerControlEnabled = on;
    }

    public RunTimeStatistics runTimeStatistics() {
        return runTimeStatistics;
    }

    protected void attemptRefitContainer(byte id) {
        if (componentManager.autoFitContainer(id))
            runTimeStatistics.componentContainerRefits++;
    }

    protected void attemptRefitPool(byte id) {
        if (componentManager.autoFitPool(id))
            runTimeStatistics.componentPoolRefits++;
    }
}
