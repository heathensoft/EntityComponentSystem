package com.nudge.ecs.gdx.systems;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.nudge.ecs.ECS;
import com.nudge.ecs.ECSystem;
import com.nudge.ecs.Entity;
import com.nudge.ecs.Getter;
import com.nudge.ecs.gdx.components.Body;
import com.nudge.ecs.gdx.components.Mortality;

/**
 * @author Frederik Dahl
 * 19/09/2021
 */


public class Renderer extends ECSystem {

     private final Getter<Body> bodyComponents;
     private final Getter<Mortality> mortalityComponents;

     private final ShapeRenderer shapeRenderer;

     public Renderer(ECS ecs, int cap) {
          super(ecs, ecs.getGroup(Body.class, Mortality.class), cap);
          mortalityComponents = ecs.getter(Mortality.class);
          bodyComponents = ecs.getter(Body.class);
          shapeRenderer = new ShapeRenderer();
     }


     @Override
     protected void processEntity(Entity e) {
          Body b = bodyComponents.get(e);
          // set color here
          shapeRenderer.circle(b.shape.x,b.shape.y,b.shape.radius);
     }


     @Override
     protected void terminate() {
          shapeRenderer.dispose();
     }

     @Override
     protected void begin() {
          Gdx.gl.glClearColor(0, 0, 0, 0);
          Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
          shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
     }

     @Override
     protected void end() {
          shapeRenderer.end();
     }
}
