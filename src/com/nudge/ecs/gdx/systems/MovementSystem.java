package com.nudge.ecs.gdx.systems;

import com.badlogic.gdx.math.Vector2;
import com.nudge.ecs.ECS;
import com.nudge.ecs.ECSystem;
import com.nudge.ecs.Entity;
import com.nudge.ecs.Getter;
import com.nudge.ecs.gdx.components.Body;
import com.nudge.ecs.gdx.components.Collider;
import com.nudge.ecs.gdx.components.Movement;

/**
 *
 * Nothing here or anywhere else in this example is not meant to be efficient.
 *
 *
 * @author Frederik Dahl
 * 19/09/2021
 */


public class MovementSystem extends ECSystem {


     private final Getter<Movement> movementComponents;
     private final Getter<Collider> colliderComponents;
     private final Getter<Body> bodyComponents;
     private final Vector2 tmp = new Vector2();

     public MovementSystem(ECS ecs, int cap) {
          super(ecs, ecs.getGroup(Movement.class,Body.class,Collider.class),cap);
          movementComponents = ecs.getter(Movement.class);
          colliderComponents = ecs.getter(Collider.class);
          bodyComponents = ecs.getter(Body.class);
     }


     @Override
     protected void processEntity(Entity e, float dt) {
          Movement m = movementComponents.get(e);
          Collider c = colliderComponents.get(e);
          Body b = bodyComponents.get(e);
          tmp.set(b.shape.x,b.shape.y);
          tmp.add(m.direction.x * dt * m.speed,
                  m.direction.y * dt * m.speed);

          b.shape.x = tmp.x;
          b.shape.y = tmp.y;
          c.range.x = tmp.x;
          c.range.y = tmp.y;

     }
}
