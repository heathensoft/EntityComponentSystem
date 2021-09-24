package com.nudge.ecs.util.containers;


import com.nudge.ecs.util.exceptions.KeyStateConflictException;

/**
 * KeyValue implementation for objects potentially inhabiting more than one KVArray.
 * To limit overhead in each item, the maximum KVArrays an item can inhabit is Byte.MAX_VALUE,
 * And the maximum number of KVArray instances at any given time is Short.MAX_VALUE.
 * I would believe both should be sufficiently high limits.
 *
 * The key and id arrays grow by their length x2 when full (starting at 2).
 * They do not shrink.
 *
 *
 * @author Frederik Dahl
 * 23/08/2021
 */


public abstract class KVShared implements KeyValue {

    private byte kvCount;
    private int[] kvKeys = {NONE,NONE};
    private short[] kvArrID = {NONE,NONE};
    
    @Override
    public int getKey(short srcArray) {
        for (byte i = 0; i < kvCount; i++) {
            if (kvArrID[i] == srcArray)
                return kvKeys[i];
        }return NONE;
    }
    
    @Override
    public void onInsert(int assignedKey, short srcArray) {
        if (kvArrID.length == kvCount) {
            if (kvCount == Byte.MAX_VALUE)
                throw new IllegalStateException("Item can max inhabit 127 Arrays");
            final int newSize = Math.min(Byte.MAX_VALUE, kvCount * 2);
            int[] newKeys = new int[newSize];
            short[] newArID = new short[newSize];
            for (byte i = 0; i < kvCount; i++) {
                newKeys[i] = kvKeys[i];
                newArID[i] = kvArrID[i];}
            for (byte i = kvCount; i < newSize; i++) {
                newKeys[i] = NONE;
                newArID[i] = NONE;}
            kvKeys = newKeys;
            kvArrID = newArID;}
        kvKeys[kvCount] = assignedKey;
        kvArrID[kvCount] = srcArray;
        kvCount++;
    }
    
    @Override
    public void onReplacement(int newKey, short srcArray) {
        for (byte i = 0; i < kvCount; i++) {
            if (kvArrID[i] == srcArray) {
                kvKeys[i] = newKey;
                return;
            }
        }throw new KeyStateConflictException("Item is not registered to inhabit the caller");
    }

    @Override
    public void onRemoval(short targetArray) {
        for (int i = 0; i < kvCount; i++) {
            if (kvArrID[i] == targetArray) {
                final int lastIndex = kvCount - 1;
                if (i != lastIndex) {
                    // here we have to put the last
                    // indexes into the removed slots.
                    kvKeys[i] = kvKeys[lastIndex];
                    kvArrID[i] = kvArrID[lastIndex];
                    kvKeys[lastIndex] = NONE;
                    kvArrID[lastIndex] = NONE;
                }
                else kvKeys[i] = kvArrID[i] = NONE;
                kvCount--;
                return;
            }
        }throw new KeyStateConflictException("Item is not registered to inhabit the caller");
    }
}
