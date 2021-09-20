package com.nudge.ecs.gdx.components;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.nudge.ecs.Component;



/**
 * @author Frederik Dahl
 * 20/09/2021
 */


public class Body implements Component {

    public static final Color RED = Color.RED;
    public static final Color WHITE = Color.WHITE;
    public static final Color GRAY = new Color(0.8f,0.8f,0.8f,1);

    public Color color;
    public Vector2 position;
    public float radius;
    public final boolean vulnerable;
    public boolean infected;

    public Body(Vector2 position, float radius, boolean vulnerable, boolean infected) {
        this.vulnerable = vulnerable;
        this.infected = infected;
        this.position = position;
        this.radius = radius;
        if (infected) color = RED;
        else if (vulnerable) color = GRAY;
        else color = WHITE;
    }


}
