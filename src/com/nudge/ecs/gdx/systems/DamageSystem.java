package com.nudge.ecs.gdx.systems;

import com.nudge.ecs.*;
import com.nudge.ecs.util.containers.Container;
import com.nudge.ecs.gdx.components.Collider;
import com.nudge.ecs.gdx.components.Mortality;

/**
 * @author Frederik Dahl
 * 19/09/2021
 */


public class DamageSystem extends ECSystem {

     private final Getter<Mortality> mortalityComponents;
     private final Getter<Collider> colliderComponents;
     private final Container<Entity> toRemove;

     public DamageSystem(ECS ecs, int cap) {
          super(ecs, ecs.getGroup(Mortality.class, Collider.class));
          mortalityComponents = ecs.getter(Mortality.class);
          colliderComponents = ecs.getter(Collider.class);
          toRemove = new Container<>(1+(cap/4));
     }


     @Override
     protected void processEntity(Entity e) {
          Collider c = colliderComponents.get(e);
          if (c.hit) {
               Mortality m = mortalityComponents.get(e);
               if (m.life == 0) {
                    toRemove.push(e);
               }
               else m.life--;
               c.hit = false;
          }
     }

     @Override
     protected void end() {
          if (toRemove.notEmpty()) {
               EntityManager em = getEcs().entityManager();
               while (toRemove.notEmpty()) {
                    em.remove(toRemove.pop());
               }
          }

     }
}
