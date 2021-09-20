package com.nudge.ecs.gdx.systems;

import com.nudge.ecs.ECS;
import com.nudge.ecs.ECSystem;
import com.nudge.ecs.Entity;
import com.nudge.ecs.Getter;
import com.nudge.ecs.gdx.components.Dying;

/**
 * @author Frederik Dahl
 * 20/09/2021
 */


public class DyingSystem extends ECSystem {

    private final Getter<Dying> dyingComponents;

    public DyingSystem(ECS ecs, int cap) {
        super(ecs,cap, ecs.getGroup(Dying.class));
        dyingComponents = ecs.getter(Dying.class);
    }

    @Override
    protected void processEntity(Entity e, float dt) {
        Dying d = dyingComponents.get(e);
        d.timeToDie -= dt;
        if (d.timeToDie < 0)
            getEcs().entityManager().remove(e);

    }
}
