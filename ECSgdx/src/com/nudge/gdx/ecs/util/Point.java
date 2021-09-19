package com.nudge.gdx.ecs.util;

public class Point<E> {

    public float x, y, r;
    public E e;

    public Point(float x, float y, float r, E element) {
        this.e = element;
        this.x = x;
        this.y = y;
        this.r = r;
    }

    public boolean intersect(Point<E> p) {
        float dx = x - p.x;
        float dy = y - p.y;
        float dist = dx * dx + dy * dy;
        float rSum = r + p.r;
        return dist < rSum * rSum;
    }

}
