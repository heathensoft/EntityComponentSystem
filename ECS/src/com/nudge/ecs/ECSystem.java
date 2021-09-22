package com.nudge.ecs;

import com.nudge.ecs.util.containers.Container;
import com.nudge.ecs.util.containers.Iterator;
import com.nudge.ecs.util.containers.KVArray;

/**
 *
 * Systems must be created before initializing the ECS.
 *
 * The initialization order:
 *
 * 1. Create the ECS
 * 2. Create the Systems and register pools
 * 3. Initialize the ECS
 * 4. Good to go
 *
 * @author Frederik Dahl
 * 18/09/2021
 */


public abstract class ECSystem {

    private final ECS ecs;
    private final KVArray<Entity> entities;
    private final Iterator<Entity> itr = this::processEntity;
    private final Container<Entity> waitToAdd = new Container<>();
    private final Container<Entity> waitToRemove = new Container<>();
    private final ComponentGroup group;
    private boolean activated;
    private boolean processing;
    private long systemBit;

    @SafeVarargs
    public ECSystem(ECS ecs, int initialCap, Class<? extends Component>... group) {
        this(ecs, initialCap, ecs.getGroup(group));
    }

    @SafeVarargs
    public ECSystem(ECS ecs, Class<? extends Component>... group) {
        this(ecs, ecs.getGroup(group));
    }

    public ECSystem(ECS ecs, int initialCap, ComponentGroup group) {
        if (ecs == null) throw new IllegalArgumentException("ECS cannot be null");
        this.entities = new KVArray<>(initialCap);
        this.group = group;
        this.ecs = ecs;
        if (ecs.isInitialized()) throw new IllegalStateException("Create system before ECS initialize");
        ecs.systemManager.register(this);
    }

    public ECSystem(ECS ecs, ComponentGroup group) {
        this(ecs,64, group);
    }

    /**
     * Called when registered in the ECS
     * @param bit the assigned system-bit
     */
    protected final void set(long bit) {
        this.systemBit = bit;
        this.activate();
    }

    public void process() {
        if (activated & !processing) {
            clean();
            begin();
            processing = true;
            entities.iterate(itr);
            processing = false;
            handleWaiting();
            end();
        }
    }

    public void process(Iterator<Entity> itr) {
        if (!processing) {
            clean();
            begin();
            processing = true;
            entities.iterate(itr);
            processing = false;
            handleWaiting();
            end();
        }
    }

    public void process(float dt) {
        if (activated & !processing) {
            clean();
            begin();
            processing = true;
            for (int i = 0; i < entities.size(); i++)
                processEntity(entities.get(i),dt);
            processing = false;
            handleWaiting();
            end();
        }
    }

    /**
     * Any state-changes to an entity (add/rem-components, enable/disable)
     * will trigger its revalidation by each registered system.
     * Systems will appropriately keep, add or remove the entity.
     *
     * If for some reason a call to entityManager.clean() should occur while the system
     * is processing entities, entities that should otherwise be immediately added or removed
     * are instead put in temporary containers until the processing completes.
     * After which they are ar handled by handleWaiting();
     *
     * @param e the entity e to be revalidated by the system
     */

    // this setup should have the least possible operations. Using positive operators only :)
    // 1.   if the entity is disabled, we only need to check if it is in the system. If it is, remove it.
    // 2.   in the case enabled == true, we first check the status-quo to see if we can return immediately.
    //      status-quo being: its both in the system and has the required components. OR the opposite.
    //      That can be simplified to: inSystem == hasComponents. (No change)
    // 3.   now we only need to know if it's in the system. if true, we know its missing the components,
    //      and therefore we remove it. else we know it meets the requirements, and we add it.

    protected final void revalidate(Entity e) {
        final boolean inSystem = e.inSystem(systemBit);
        if (e.isEnabled()) {
            if (inSystem == group.containsAll(e.components())) return;
            if (inSystem) removeEntity(e);
            else addEntity(e);
        } else if (inSystem) removeEntity(e);
    }

    private void addEntity(Entity e) {
        e.addSystem(systemBit);
        if (processing)
            waitToAdd.push(e);
        else {
            entities.add(e);
            entityAdded(e);
        }
    }

    private void removeEntity(Entity e) {
        e.removeSystem(systemBit);
        if (processing)
            waitToRemove.push(e);
        else {
            entities.remove(e);
            entityRemoved(e);
        }
    }



    protected void processEntity(Entity e) {}

    protected void processEntity(Entity e, float dt) {}

    protected void entityAdded(Entity e) {}

    protected void entityRemoved(Entity e) {}

    protected void initialize() {}

    protected void terminate() {}

    protected void begin() {}

    protected void end() {}



    protected ECS getEcs() {
        return ecs;
    }

    protected void clean() {
        ecs.entityManager.clean();
    }

    /**
     * Handles any entities tried to be added / removed during processing.
     * That in itself should not really happen if done from the same thread as the system.
     * Since any calls to entityManager.clean() happens before every ECSystem entity processing.
     * But if it should happen, the entities are put in "waiting queues" that are handled
     * straight after processing (this method). The entities are also checked to see if no
     * state-change has happened during the time in waiting. I doubt this would happen.
     * But it theoretically could. Perhaps it was added or removed again while waiting.
     * Either way, its state is double-checked and handled appropriately.
     */

    protected void handleWaiting() {

        while (waitToAdd.notEmpty()) {
            Entity e = waitToAdd.pop();
            if (e.inSystem(systemBit)) {
                entities.add(e);
                entityAdded(e);
            }
        }
        while (waitToRemove.notEmpty()) {
            Entity e = waitToRemove.pop();
            if (!e.inSystem(systemBit)) {
                entities.remove(e);
                entityRemoved(e);
            }
        }
    }

    protected KVArray<Entity> getEntities() {
        return entities;
    }

    public void activate() {
        activated = true;
    }

    public void deactivate() {
        activated = false;
    }

    public boolean isActivated() {
        return activated;
    }

    public boolean isProcessing() {
        return processing;
    }

    public long getSystemBit() {
        return systemBit;
    }

    public ComponentGroup getGroup() {
        return group;
    }
}
