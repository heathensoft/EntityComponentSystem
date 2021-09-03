package ecs;


/**
 * @author Frederik Dahl
 * 01/09/2021
 */


public class ComponentType {

    private final Class<? extends Component> componentClass;
    private final long flag;
    private final byte id;
    private String name;


    protected ComponentType(Class<? extends Component> c, long flag, byte id) {
        this.componentClass = c;
        this.flag = flag;
        this.id = id;
        name = "ComponentType_" + id;
    }

    public String name() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long flag() {
        return flag;
    }

    public Class<? extends Component> componentClass() {
        return componentClass;
    }

    public byte id() {
        return id;
    }
}
