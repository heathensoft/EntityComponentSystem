package com.nudge.ecs.gdx.components;

import com.nudge.ecs.Component;
import com.nudge.ecs.gdx.Lab;

/**
 * @author Frederik Dahl
 * 20/09/2021
 */


public class Dying implements Component {

    public Dying(float time) {
        this.timeLeft = time;
    }

    public Dying() {
        this(Lab.TIME_TO_DIE);
    }

    public float timeLeft;
}
