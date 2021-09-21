package com.nudge.ecs.gdx.util;

import com.badlogic.gdx.math.Circle;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;

/**
 * A quadtree used for collision checks.
 * With this, "particles" only need to check their immediate surroundings for collision.
 * Making checks a lot faster.
 *
 * @author Frederik Dahl
 * 2019
 */

public class QuadTree<E> {

    private final float x,y,w,h;
    private boolean divided;
    private final Array<Point<E>> points;
    private QuadTree<E>[] regions;

    public static final int NW = 0;
    public static final int NE = 1;
    public static final int SW = 2;
    public static final int SE = 3;
    public static final int CAP = 5;

    public QuadTree(float x, float y, float w, float h) {

        points = new Array<>();
        this.x = x;
        this.y = y;
        this.w = w;
        this.h = h;
    }

    public void insert(Point<E> p) {

        if(!this.contains(p.x, p.y)) return;
        if(points.size < CAP) points.add(p);
        else{
            if (!divided)split();
            regions[NW].insert(p);
            regions[NE].insert(p);
            regions[SW].insert(p);
            regions[SE].insert(p);
        }
    }
    
    private void split() {

        divided = true;
        float childW = w/2;
        float childH = h/2;
        regions = new QuadTree[4];
        regions[SW] = new QuadTree<>(x, y, childW, childH);
        regions[SE] = new QuadTree<>(x + childW, y, childW, childH);
        regions[NW] = new QuadTree<>(x, y + childH, childW, childH);
        regions[NE] = new QuadTree<>(x + childW, y + childH, childW, childH);
    }

    public void query(Array<E> list, Rectangle box) {

        if(this.overlaps(box)) {
            for (int i = 0; i < points.size; i++) {
                if (box.contains(points.get(i).x, points.get(i).y)) {
                    list.add(points.get(i).e);
                }
            }
            if(divided) {
                regions[NW].query(list, box);
                regions[NE].query(list, box);
                regions[SW].query(list, box);
                regions[SE].query(list, box);
            }
        }
    }

    public void query(Array<E> list, Circle circle) {

        if(this.overlaps(circle)) {
            for (int i = 0; i < points.size; i++) {
                if (circle.contains(points.get(i).x, points.get(i).y)) {
                    list.add(points.get(i).e);
                }
            }
            if(divided) {
                regions[NW].query(list, circle);
                regions[NE].query(list, circle);
                regions[SW].query(list, circle);
                regions[SE].query(list, circle);
            }
        }
    }

    // a "Point" can't inhabit than one region
    private boolean contains(float px, float py) {
        return x <= px && x + w > px && y <= py && y + h > py;
    }

    private boolean overlaps(Rectangle r) {
        return x < r.x + r.width && x + w > r.x && y < r.y + r.height && y + h > r.y;
    }

    private boolean overlaps(Circle c) {

        float closestX = c.x;
        float closestY = c.y;

        if (c.x < x) closestX = x;
        else if (c.x > x + w) closestX = x + w;

        if (c.y < y) closestY = y;
        else if (c.y > y + h) closestY = y + h;

        closestX = closestX - c.x;
        closestX *= closestX;
        closestY = closestY - c.y;
        closestY *= closestY;

        return closestX + closestY < c.radius * c.radius;
    }
}
