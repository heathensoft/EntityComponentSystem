package com.nudge.ecs.gdx.systems;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.nudge.ecs.*;
import com.nudge.ecs.gdx.components.Body;

/**
 * @author Frederik Dahl
 * 20/09/2021
 */


public class Renderer extends ECSystem {

    private final ShapeRenderer shapeRenderer;
    private final Getter<Body> bodyComponents;

    public Renderer(ECS ecs, int cap) {
        super(ecs, cap, ecs.getGroup(Body.class));
        bodyComponents = ecs.getter(Body.class);
        shapeRenderer = new ShapeRenderer();
    }

    @Override
    protected void processEntity(Entity e) {
        Body b = bodyComponents.getUnsafe(e);
        shapeRenderer.setColor(b.color);
        shapeRenderer.circle(
                b.position.x,
                b.position.y,
                b.radius);
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
