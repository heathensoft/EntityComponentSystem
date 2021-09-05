package ecs;

/**
 * @author Frederik Dahl
 * 01/09/2021
 */


public class ComponentGroup {

    private String name;
    protected final long mask;
    protected final short id;

    protected ComponentGroup(long mask, short id) {
        this.mask = mask;
        this.id = id;
        name = "ComponentGroup_" + id;
    }

    public boolean isMember(Entity e) {
        return containsAll(e.components());
    }

    public String name() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int id() {
        return id;
    }

    protected boolean match(long bits) {
        return mask == bits;
    }

    protected boolean containsAny(long bits) {
        return (bits & mask) > 0;
    }

    protected boolean containsAll(long bits) {
        return mask == (mask & bits);
    }

    protected long mask() {
        return mask;
    }
}
