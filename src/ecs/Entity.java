package ecs;


import ecs.util.KVShared;

/**
 * @author Frederik Dahl
 * 21/08/2021
 *
 */


public class Entity extends KVShared {
    

    private final EntityManager entityManager;
    private final ComponentManager componentManager;

    private final int id;
    private long typeFlags;
    private long systemFlags;

    boolean enabled;
    boolean dirty;


    protected Entity(EntityManager entityManager, ComponentManager componentManager, int id) {
        this.entityManager = entityManager;
        this.componentManager = componentManager;
        this.id = id;;
    }


    public void enable() {
        if (!isEnabled())
            refresh();
        enabled = true;
    }

    public void disable(){
        if (isEnabled())
            refresh();
        enabled = false;
    }

    public void refresh() {
        if (isDirty()) return;
        //manager.addToDirty(this);
        dirty = true;
    }

    protected void refreshed() {
        if (!isDirty()) return;
        dirty = false;
    }

    public void reset() {

    }

    public boolean isEnabled() {
        return enabled;
    }

    public boolean isDirty() {
        return dirty;
    }

    public int id() {
        return id;
    }

    protected long typeFlags() {
        return typeFlags;
    }
    
    protected void addTypeFlag(long bit) {
        typeFlags |= bit;
    }

    protected void setTypeFlags(long typeFlags) {
        this.typeFlags = typeFlags;
    }
    
    protected void removeTypeFlag(long bit) {
        typeFlags &= ~bit;
    }
    
    protected long systemFlags() {
        return systemFlags;
    }
    
    protected void addSystemFlag(long bit) {
        systemFlags |= bit;
    }

    protected void setSystemFlags(long systemFlags) {
        this.systemFlags = systemFlags;
    }
    
    protected void removeSystemFlag(long bit) {
        systemFlags &= ~bit;
    }


    public void printGroups() {
        System.out.println(this);
        //ComponentGroup.printGroups(typeFlags);
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
