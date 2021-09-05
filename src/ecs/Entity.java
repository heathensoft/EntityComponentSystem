package ecs;


import ecs.util.KVShared;

/**
 * @author Frederik Dahl
 * 21/08/2021
 *
 */


public class Entity extends KVShared {

    protected final int id;
    private long systems = 0L;
    private long components = 0L;
    private long enabledComponents = 0L;
    protected boolean enabled = true;
    protected boolean dirty = false;

    protected Entity( int id) { this.id = id; }

    public void reset() {
        enabled = true;
        dirty = false;
        systems = 0L;
        components = 0L;
        enabledComponents = 0L;
    }

    public int id() {
        return id;
    }

    public boolean isDirty() {
        return dirty;
    }

    public boolean isEnabled() {
        return enabled;
    }

    protected long systems() {
        return systems;
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

    protected long components() {
        return components;
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

    protected long enabledComponents() {
        return enabledComponents;
    }

    protected boolean isComponentEnabled(long bit) {
        return (enabledComponents & bit) == bit;
    }

    protected void enableComponent(long bit) {
        enabledComponents |= bit;
    }

    protected void setEnabledComponents(long bits) {
        enabledComponents = bits;
    }

    protected void disableComponent(long bit) {
        enabledComponents &= ~bit;
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
