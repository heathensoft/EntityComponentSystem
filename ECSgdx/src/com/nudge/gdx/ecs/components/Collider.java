package com.nudge.gdx.ecs.components;

import com.badlogic.gdx.math.Circle;
import com.nudge.ecs.Component;

/**
 * @author Frederik Dahl
 * 19/09/2021
 */


public class Collider implements Component {

    public boolean hit;
    public Circle range;
}
