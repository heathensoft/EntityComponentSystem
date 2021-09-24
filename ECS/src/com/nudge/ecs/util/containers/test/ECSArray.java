package com.nudge.ecs.util.containers.test;

import com.nudge.ecs.util.containers.Iterator;

/**
 *
 * Shared traits of all ECS internal item containers.
 *
 * !! target capacity is important for all ECSArrays. They are auto-resizable
 * and the targetCap value is the "restingCap" for the arrays.
 * In most cases, when you remove the final item and the array length is greater
 * than the targetCap it will shrink back to that "restingCap".
 * The fit() method too, will consider the targetCap. Unless you fit absolute.
 * You can set the targetCap after creating a ECSArray.
 *
 * Container: Used as a stack or indexed array
 * Queue: Used as a queue
 * KVArray: Used instead of HashMap to have both fast iteration and removal.
 *
 * @author Frederik Dahl
 * 23/09/2021
 */


public interface ECSArray<E> {

    int MIN_CAPACITY = 8;
    int DEFAULT_CAPACITY = 16;

    void iterate(Iterator<E> itr);

    void clear();

    void ensureCapacity(int n);

    boolean fit(boolean absolute);

    int count();

    float loadFactor();

    int capacity();

    int targetCapacity();

    void setTargetCapacity(int cap);

    boolean isEmpty();

    boolean notEmpty();

    default int growFormula(int n) {
        return ((n + 1) * 3) / 2 + 1;
    }

}
