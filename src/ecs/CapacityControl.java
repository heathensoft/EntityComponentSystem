package ecs;

import java.util.Arrays;

/**
 * The MemoryManager is not strictly necessary for most use-cases.
 * But it is useful with a large amount of entities and components being
 * created and destroyed in a long-running application.
 * It keeps track of parts of the system that's been inactive for a while (5 min)
 * then tries to minimize the memory footprint of pools and containers.
 * It won't call the garbage collector explicitly.
 *
 * It also keeps track of run-time ECS statistics for debugging purposes.
 * It runs in 20 seconds time-step. Some component stats have a 20sec delay.
 *
 * Timers are added automatically when a new ComponentType has been created by the ComponentManager.
 * Specific container timers are started / reset when a new component of given type are added to
 * the ComponentManager. If a container has been inactive for 5 minutes. It will check if
 * it's possible to resize the container. That happens if the capacity > target capacity.
 * It will never shrink containers below that target.
 * After a check, the timer will pause until triggered again.
 * In addition to component containers, if you add pools to the system. The MemoryManager will
 * query the pools in the same manner.
 *
 *
 * There are two timers for each index of the short[]:
 *
 * LSB - Containers (4bit - 15 time-steps of 20 seconds)
 * MSB - Pools      (4bit - 15 time-steps of 20 seconds)
 *
 * The index is the ComponentTypes' id.
 * @author Frederik Dahl
 * 09/09/2021
 */


public class CapacityControl {

    private static final short CONTAINER_CHECKED    = 0x0F;
    private static final short POOL_CHECKED         = 0xF0;
    private static final short UP_TO_DATE           = 0xFF;
    private static final short INTERVAL             = 0x14;

    private final short[] timers;
    private float accumulator;
    private byte timerCount;

    private final ComponentManager manager;

    protected CapacityControl(ComponentManager componentManager) {
        this.manager = componentManager;
        timers = new short[Long.SIZE];
        Arrays.fill(timers,UP_TO_DATE);
    }

    protected void check(float dt) {
        accumulator += dt;
        if (accumulator > INTERVAL) {
            accumulator -= INTERVAL;
            for (byte i = 0; i < timerCount; i++) {
                if (timers[i] != UP_TO_DATE) {
                    if (!containerTimerMaxed(timers[i]))
                        if (containerTimerMaxed(++timers[i]))
                            manager.attemptRefitContainer(i);
                    if (!poolTimerMaxed(timers[i]))
                        if (poolTimerMaxed(timers[i] += 0x10))
                            manager.attemptRefitPool(i);
                }
            }
        }
    }

    protected void newTimer() {
        timerCount++;
    }

    protected void resetContainerTimer(byte index) {
        timers[index] &= ~CONTAINER_CHECKED;
    }

    protected void resetPoolTimer(byte index) {
        timers[index] &= ~POOL_CHECKED;
    }

    private boolean containerTimerMaxed(short timer) {
        return (timer & CONTAINER_CHECKED) == CONTAINER_CHECKED;
    }

    private boolean poolTimerMaxed(short timer) {
        return (timer & POOL_CHECKED) == POOL_CHECKED;
    }
}
