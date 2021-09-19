package com.nudge.gdx.ecs.systems;

import com.nudge.ecs.ECS;
import com.nudge.ecs.ECSystem;
import com.nudge.gdx.ecs.components.Movement;

/**
 * @author Frederik Dahl
 * 19/09/2021
 */


public class MovementSystem extends ECSystem {


     public MovementSystem(ECS ecs, int cap) {
          super(ecs, ecs.getGroup(Movement.class),cap);
     }
}
