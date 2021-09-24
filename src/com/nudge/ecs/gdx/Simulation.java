package com.nudge.ecs.gdx;


import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.nudge.ecs.ECS;
import com.nudge.ecs.ECSystem;
import com.nudge.ecs.gdx.systems.CollisionSystem;
import com.nudge.ecs.gdx.systems.DyingSystem;
import com.nudge.ecs.gdx.systems.MovementSystem;
import com.nudge.ecs.gdx.systems.Renderer;
import com.nudge.ecs.gdx.util.Screenshot;
import com.nudge.ecs.util.IntQueue;
import com.nudge.ecs.util.containers.Queue;

/**
 * @author Frederik Dahl
 * 18/09/2021
 */


public class Simulation extends InputAdapter {

    // -------------------------------------------------------
    ECS ecs;                                               //|
    Lab lab;                                               //|
    // --------------------- Systems -------------------------
    ECSystem renderer;                                     //|
    ECSystem collisionSystem;                              //|
    ECSystem movementSystem;                               //|
    ECSystem dyingSystem;                                  //|
    // -------------------------------------------------------

    public void initialize() {

        final int initialCap = 1000;

        // Setting up the ECS and creating the "lab"
        ecs = new ECS(initialCap);
        lab = new Lab(ecs.entityManager());

        // Creating our systems
        movementSystem = new MovementSystem(ecs,initialCap);
        collisionSystem = new CollisionSystem(ecs,initialCap);
        dyingSystem = new DyingSystem(ecs,initialCap);
        renderer = new Renderer(ecs,initialCap);

        // Initialize the ECS
        ecs.initialize();
        // creating our entities in the lab
        lab.createVulnerable(15000);
        // libgdx input
        Gdx.input.setInputProcessor(this);
    }

    // Processing our systems
    public void update(float dt) {
        Gdx.graphics.setTitle("FPS: "+(int)(1/dt));
        dyingSystem.process(dt);
        collisionSystem.process();
        movementSystem.process(dt);
        ecs.capacityControl(dt); //
    }

    // Processing the rendering system after update
    public void render() {
        renderer.process();
    }

    // Always terminate the ECS
    public void dispose() {
        ecs.terminate();
    }


    // Introduce a virus (infected entity) at the mouse-leftClick coordinate
    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        screenY = Gdx.graphics.getHeight() - screenY;
        if(pointer == 0 && button == 0){
            lab.introduceVirus(screenX,screenY);
        }return true;
    }

    @Override
    public boolean keyDown(int keycode) {
        if (keycode == Input.Keys.ESCAPE) {
            Gdx.app.exit();
        }
        else if (keycode == Input.Keys.S) {
            Screenshot.snap();
        }
        return true;
    }
}
