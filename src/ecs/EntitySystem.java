package ecs;


/**
 * @author Frederik Dahl
 * 20/08/2021
 */


public abstract class EntitySystem {
    

    private final ECS ecs;
    private EntityManager entityManager; // final
    
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

    protected void revalidate(Entity e) {

    }
    

    protected void initialize() {

    }

    public final void update() {
        if (activated) {
            begin();
            process();
            entityManager.clean();
            end();
        }
    }

    protected void terminate() {

    }

    protected void begin() {

    }

    protected void process() {

    }

    protected void end() {

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

    protected void setSystemBit(long bit) {
        this.systemBit = bit;
    }

    protected long getSystemBit() {
        return systemBit;
    }

    public ComponentGroup getGroup() {
        return group;
    }

}
