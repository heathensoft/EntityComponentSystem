package com.nudge.ecs.util.containers;

/**
 * @author Frederik Dahl
 * 29/08/2021
 */

@FunctionalInterface
public interface Iterator<E> {
    void next(E item);
}
