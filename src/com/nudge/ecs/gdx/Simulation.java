package com.nudge.ecs.gdx;


import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputAdapter;
import com.nudge.ecs.ECS;
import com.nudge.ecs.gdx.systems.CollisionSystem;
import com.nudge.ecs.gdx.systems.DyingSystem;
import com.nudge.ecs.gdx.systems.MovementSystem;
import com.nudge.ecs.gdx.systems.Renderer;

/**
 * @author Frederik Dahl
 * 18/09/2021
 */


public class Simulation extends InputAdapter {


    ECS ecs;
    Lab lab;

    // Systems
    Renderer renderer;
    CollisionSystem collisionSystem;
    MovementSystem movementSystem;
    DyingSystem dyingSystem;


    public void initialize() {
        final int initialCap = 40000;
        ecs = new ECS(initialCap);
        lab = new Lab(ecs.entityManager());
        movementSystem = new MovementSystem(ecs,initialCap);
        collisionSystem = new CollisionSystem(ecs,initialCap);
        dyingSystem = new DyingSystem(ecs,initialCap);
        renderer = new Renderer(ecs,initialCap);
        ecs.registerSystem(movementSystem);
        ecs.registerSystem(collisionSystem);
        ecs.registerSystem(dyingSystem);
        ecs.registerSystem(renderer);
        ecs.initialize();
        Gdx.input.setInputProcessor(this);
        lab.createVulnerable(initialCap);
    }

    public void update(float dt) {
        Gdx.graphics.setTitle("FPS: "+(int)(1/dt));
        //dyingSystem.process(dt);
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
            lab.introduceVirus(screenX,screenY);
        } else if (pointer == 0 && button == 1){
            //System.out.println(screenX + " " + screenY);
        }return true;
    }


}
