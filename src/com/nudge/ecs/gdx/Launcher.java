package com.nudge.ecs.gdx;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;

/**
 * @author Frederik Dahl
 * 18/09/2021
 */


public class Launcher {

    public static void main(String[] args) {

        LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
        config.width = 1280;
        config.height = 720;
        config.resizable = false;
        config.vSyncEnabled = true;
        //config.foregroundFPS = 0;
        //config.backgroundFPS = 0;

        new LwjglApplication(

                new ApplicationAdapter() {

            Simulation simulation;
            @Override
            public void create() {
                simulation = new Simulation();
                simulation.initialize();
            }

            @Override
            public void render() {
                simulation.update(Gdx.graphics.getDeltaTime());
                simulation.render();
            }

            @Override
            public void dispose() {
                simulation.dispose();
            }
        }, config);

    }
}
