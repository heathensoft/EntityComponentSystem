package ecs;

import java.util.Arrays;

/**
 * @author Frederik Dahl
 * 09/09/2021
 */


public class ContainerControl {

    private static final short TIME_STEP            = 0x14;
    private static final short CONTAINER_CHECKED    = 0x0F;
    private static final short POOL_CHECKED         = 0xF0;
    private static final short UP_TO_DATE           = 0xFF;

    private final short[] timers;
    private float accumulator;
    private byte timerCount;

    private final Memory memory;

    protected ContainerControl(Memory memory) {
        timers = new short[Long.SIZE];
        Arrays.fill(timers,UP_TO_DATE);
        this.memory = memory;
    }

    protected void check(float dt) { // could do the accumulation in the memory manager. yes do that
        accumulator += dt;
        if (accumulator > TIME_STEP) {
            accumulator -= TIME_STEP;
            for (byte i = 0; i < timerCount; i++) {
                if (timers[i] != UP_TO_DATE) {
                    if (!containerTimerMaxed(timers[i]))
                        if (containerTimerMaxed(++timers[i]))
                            memory.attemptRefitContainer(i);
                    if (!poolTimerMaxed(timers[i]))
                        if (poolTimerMaxed(timers[i] += 0x10))
                            memory.attemptRefitPool(i);
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
