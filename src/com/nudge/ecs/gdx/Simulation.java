package com.nudge.ecs.gdx;


import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputAdapter;
import com.nudge.ecs.ECS;
import com.nudge.ecs.gdx.systems.CollisionSystem;
import com.nudge.ecs.gdx.systems.DamageSystem;
import com.nudge.ecs.gdx.systems.MovementSystem;
import com.nudge.ecs.gdx.systems.Renderer;

/**
 * @author Frederik Dahl
 * 18/09/2021
 */


public class Simulation extends InputAdapter {


    ECS ecs;
    Forge forge;
    Renderer renderer;
    MovementSystem movementSystem;
    CollisionSystem collisionSystem;
    DamageSystem damageSystem;

    public void initialize() {
        final int initialCap = 1000;
        ecs = new ECS(initialCap,Short.MAX_VALUE);
        movementSystem = new MovementSystem(ecs,initialCap);
        collisionSystem = new CollisionSystem(ecs,initialCap);
        damageSystem = new DamageSystem(ecs,initialCap);
        renderer = new Renderer(ecs,initialCap);
        ecs.registerSystem(movementSystem);
        ecs.registerSystem(collisionSystem);
        ecs.registerSystem(damageSystem);
        ecs.registerSystem(renderer);
        forge = new Forge(ecs.entityManager());
        ecs.initialize();
        Gdx.input.setInputProcessor(this);

    }

    public void update(float dt) {
        damageSystem.process();
        collisionSystem.process();
        movementSystem.process(dt);
    }

    public void render() {
        renderer.process();
    }

    public void dispose() {
        ecs.terminate();
    }




    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        screenY = Gdx.graphics.getHeight() - screenY;
        if(pointer == 0 && button == 0){
            forge.create(screenX,screenY);
            System.out.println(screenX + " " + screenY);
        } else if (pointer == 0 && button == 1){
            System.out.println(screenX + " " + screenY);
        }
        return true;
    }


}
