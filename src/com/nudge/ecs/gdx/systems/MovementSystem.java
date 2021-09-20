package com.nudge.ecs.gdx.systems;

import com.badlogic.gdx.math.Vector2;
import com.nudge.ecs.ECS;
import com.nudge.ecs.ECSystem;
import com.nudge.ecs.Entity;
import com.nudge.ecs.Getter;
import com.nudge.ecs.gdx.components.Body;
import com.nudge.ecs.gdx.components.Velocity;

/**
 * @author Frederik Dahl
 * 20/09/2021
 */


public class MovementSystem extends ECSystem {

    private final Getter<Body> bodyComponents;
    private final Getter<Velocity> velocityComponents;
    private final Vector2 tmp = new Vector2();

    public MovementSystem(ECS ecs, int cap) {
        super(ecs, cap, Velocity.class, Body.class);
        velocityComponents = ecs.getter(Velocity.class);
        bodyComponents = ecs.getter(Body.class);
    }

    @Override
    protected void processEntity(Entity e, float dt) {
        Body b = bodyComponents.getUnsafe(e);
        Velocity v = velocityComponents.getUnsafe(e);
        tmp.set(v.velocity).scl(dt);
        b.position.add(tmp);
    }
}
