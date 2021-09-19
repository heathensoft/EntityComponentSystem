package com.nudge.ecs.gdx;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Circle;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.nudge.ecs.Entity;
import com.nudge.ecs.EntityManager;
import com.nudge.ecs.gdx.components.Body;
import com.nudge.ecs.gdx.components.Collider;
import com.nudge.ecs.gdx.components.Mortality;
import com.nudge.ecs.gdx.components.Movement;

/**
 * @author Frederik Dahl
 * 20/09/2021
 */


public class Forge {

     private EntityManager entityManager;

     public Forge(EntityManager entityManager){
          this.entityManager = entityManager;
     }

     public void create(int x, int y) {

          Entity e = entityManager.create();

          Body body = new Body();
          body.shape = new Circle(x,y,16);

          Collider collider = new Collider();
          collider.hit = false;
          collider.range = new Circle(0,0,body.shape.radius*2);

          Mortality mortality = new Mortality();

          Movement movement = new Movement();
          movement.direction = rngDir();
          movement.speed = 10;

          entityManager.addComponents(e,body,collider,mortality,movement);
     }

     private Vector2 rngDir() {
          Vector2 v = new Vector2(1,0);
          v.rotate(MathUtils.random(0,360));
          //v.nor();
          return  v;
     }

     private float rngRadius() {
          return MathUtils.random(8);
     }

     public Vector2 rngPos() {
          int screenW = Gdx.graphics.getWidth();
          int screenH = Gdx.graphics.getHeight();
          return new Vector2(MathUtils.random(0,screenW),MathUtils.random(0,screenH));
     }
}
