package com.nudge.ecs.gdx;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.nudge.ecs.Entity;
import com.nudge.ecs.EntityManager;
import com.nudge.ecs.gdx.components.Body;
import com.nudge.ecs.gdx.components.Collider;
import com.nudge.ecs.gdx.components.Dying;
import com.nudge.ecs.gdx.components.Velocity;

import java.util.Random;

/**
 * @author Frederik Dahl
 * 20/09/2021
 */


public class Lab {

     private final EntityManager manager;
     private final Random rnd;

     private final float speed = 10.0f;
     private final int maxRadius = 4;
     public final static int TIME_TO_DIE = 60;

     public Lab(EntityManager entityManager){
          this.manager = entityManager;
          this.rnd = new Random();
     }

     public void introduceVirus(int x, int y) {
          Entity e = manager.create();
          //System.out.println(e.id());
          Body body = new Body(new Vector2(x,y), rngRadius(),true,true);
          Velocity velocity = new Velocity(rnd.nextFloat(), speed);
          manager.addComponents(e, body, new Collider(), velocity, new Dying());
     }

     public void createVulnerable(int amount) {
          amount = Math.max(1,amount);
          for (int i = 0; i < amount; i++) {
               Entity e = manager.create();
               Body body = new Body(rngPos(), rngRadius(),true,false);
               Velocity velocity = new Velocity(rnd.nextFloat(), speed);
               manager.addComponents(e, body, new Collider(), velocity);
          }
     }

     public void createImmune(int amount) {
          amount = Math.max(1,amount);
          for (int i = 0; i < amount; i++) {
               Entity e = manager.create();
               Body body = new Body(rngPos(), rngRadius(),false,false);
               Velocity velocity = new Velocity(rnd.nextFloat(), speed);
               manager.addComponents(e, body, new Collider(), velocity);
          }
     }

     private Vector2 rngDir() {
          Vector2 v = new Vector2(1,0);
          return v.rotate(MathUtils.random(0,360));
     }

     private float rngRadius() {
          return Math.max(MathUtils.random(maxRadius),1);
     }

     public Vector2 rngPos() {
          int w = Gdx.graphics.getWidth();
          int h = Gdx.graphics.getHeight();
          return new Vector2(MathUtils.random(0,w),MathUtils.random(0,h));
     }
}
