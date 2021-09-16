package ecs.util.containers;

import ecs.util.containers.exceptions.KeyStateConflictException;

/**
 * @author Frederik Dahl
 * 29/08/2021
 */


public interface KeyValue {

    byte NONE = -1;

    /**
     * @param srcArray Caller Array
     * @return The key if the srcArray is identified by KeyValue object, or NONE (-1)
     */
    int getKey(short srcArray);

    void onInsert(int assignedKey, short srcArray);

    void onReplacement(int newKey, short srcArray) throws KeyStateConflictException;

    void onRemoval(short targetArray) throws KeyStateConflictException;
}
