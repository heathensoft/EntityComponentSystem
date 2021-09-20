package com.nudge.ecs.gdx.components;

import com.badlogic.gdx.math.Vector2;
import com.nudge.ecs.Component;

/**
 * @author Frederik Dahl
 * 20/09/2021
 */


public class Velocity implements Component {

    public final Vector2 velocity;
    public final float speed;

    public Velocity(float radians, float speed) {
        velocity = new Vector2(speed,0);
        velocity.rotateRad(radians);
        this.speed = speed;
    }


}
