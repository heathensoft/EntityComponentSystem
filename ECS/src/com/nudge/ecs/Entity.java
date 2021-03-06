package com.nudge.ecs;


import com.nudge.ecs.util.containers.KVShared;

/**
 * @author Frederik Dahl
 * 21/08/2021
 *
 */


public class Entity extends KVShared {

    private final int id;
    private long systems;
    private long components;
    private boolean enabled;
    private boolean dirty;

    protected Entity( int id) {
        this.id = id;
        reset();
    }

    public void reset() {
        enabled = true;
        dirty = false;
        systems = 0L;
        components = 0L;
    }

    public int id() {
        return id;
    }

    public boolean isDirty() {
        return dirty;
    }

    protected void markAsDirty() {
        dirty = true;
    }

    protected void markAsClean() {
        dirty = false;
    }

    public boolean isEnabled() {
        return enabled;
    }

    protected void disable() {
        enabled = false;
    }

    protected void enable() {
        enabled = true;
    }

    public long systemCount() {
        return Long.bitCount(systems);
    }

    public long componentCount() {
        return Long.bitCount(components);
    }

    public long systems() {
        return systems;
    }

    public long components() {
        return components;
    }

    protected boolean inSystem(long bit) {
        return (systems & bit) == bit;
    }

    protected void addSystem(long bit) {
        systems |= bit;
    }

    protected void setSystems(long bits) {
        systems = bits;
    }

    protected void removeSystem(long bit) {
        systems &= ~bit;
    }

    protected boolean hasAnyComponent() {
        return components > 0;
    }

    protected boolean hasComponent(long bit) {
        return (components & bit) == bit;
    }
    
    protected void addComponent(long bit) {
        components |= bit;
    }

    protected void setComponents(long bits) {
        components = bits;
    }
    
    protected void removeComponent(long bit) {
        components &= ~bit;
    }

    @Override
    public int hashCode() {
        return id % 10000;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Entity) {
            Entity other = (Entity) obj;
            return this.id == other.id;
        }return false;
    }

    @Override
    public String toString() {
        return "[Entity: " + id + "]";
    }
}
