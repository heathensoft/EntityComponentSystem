package ecs;


/**
 * @author Frederik Dahl
 * 20/08/2021
 */


public abstract class EntitySystem {
    

    private final ECS ecs;
    
    private final ComponentGroup group;
    private long systemBit;
    private boolean activated;
    
    
    @SafeVarargs
    public EntitySystem(ECS ecs, Class<? extends Component>... types) {
        this(ecs,ecs.componentManager.getGroup(types));
    }
    
    public EntitySystem(ECS ecs, ComponentGroup group) {
        this.ecs = ecs;
        this.group = group;

    }
    

    protected void initialize() {

    }

    public final void process() {
        if (activated) {
            preProcess();
            processEntities();
            postProcess();
        }
    }

    protected void terminate() {

    }

    protected void preProcess() {

    }

    protected void processEntities() {

    }

    protected void postProcess() {

    }
    
    public void activate() {
        activated = true;
    }

    public void deactivate() {
        activated = false;
    }

    private void addEntity(Entity e) {

    }

    private void removeEntity(Entity e) {

    }

    private void enableEntity(Entity e) {

    }

    private void disableEntity(Entity e) {

    }

    protected void setSystemBit(long bit) {
        this.systemBit = bit;
    }

    public long getSystemBit() {
        return systemBit;
    }

    public ComponentGroup getGroup() {
        return group;
    }

}
