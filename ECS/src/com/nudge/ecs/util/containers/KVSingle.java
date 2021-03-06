package com.nudge.ecs.util.containers;


/**
 * @author Frederik Dahl
 * 30/08/2021
 */


public abstract class KVSingle implements KeyValue {

    private int kvKey = KeyValue.NONE;

    @Override
    public int getKey(short srcArray) {
        return kvKey;
    }

    @Override
    public void onInsert(int assignedKey, short srcArray) {
        kvKey = assignedKey;
    }

    @Override
    public void onReplacement(int newKey, short srcArray) {
        kvKey = newKey;
    }

    @Override
    public void onRemoval(short targetArray) {
        kvKey = KeyValue.NONE;
    }
}
