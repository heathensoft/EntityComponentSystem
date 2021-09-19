package com.nudge.ecs.gdx.systems;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Circle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.nudge.ecs.ECS;
import com.nudge.ecs.ECSystem;
import com.nudge.ecs.Entity;
import com.nudge.ecs.Getter;
import com.nudge.ecs.util.containers.Iterator;
import com.nudge.ecs.gdx.components.Body;
import com.nudge.ecs.gdx.components.Collider;
import com.nudge.ecs.gdx.components.Movement;
import com.nudge.ecs.gdx.util.Point;
import com.nudge.ecs.gdx.util.QuadTree;

/**
 * @author Frederik Dahl
 * 19/09/2021
 */


public class CollisionSystem extends ECSystem {

    private QuadTree<Entity> quadTree;
    private final Vector2 tmp = new Vector2();
    private final Array<Entity> queried;
    private final Getter<Body> bodyComponents;
    private final Getter<Movement> movementComponents;
    private final Getter<Collider> colliderComponents;

    private final Iterator<Entity> treeInit = new Iterator<>() {
        @Override
        public void next(Entity e) {

            Body b = bodyComponents.get(e);
            quadTree.insert(new Point<>(
                    b.shape.x,
                    b.shape.y,
                    b.shape.radius,
                    e
            ));
        }
    };


    public CollisionSystem(ECS ecs,int cap) {
        super(ecs, ecs.getGroup(Collider.class, Body.class, Movement.class), cap);
        bodyComponents = ecs.getter(Body.class);
        colliderComponents = ecs.getter(Collider.class);
        movementComponents = ecs.getter(Movement.class);
        queried = new Array<>(cap);
    }

    @Override
    protected void processEntity(Entity e) {

        Collider collider = colliderComponents.get(e);
        checkBounds(e,collider);
        if (!collider.hit){ // if colliding with bounds, don't check other collisions. just testing
            quadTree.query(queried,collider.range);
            for (Entity o: queried) {
                if (!o.equals(e))
                    collisionCheck(e, o, collider);
            }
            queried.clear();
        }

    }

    private void checkBounds(Entity e, Collider c) {
        Body b = bodyComponents.get(e);

        boolean invert, invertX, invertY;
        invert = invertX = invertY = false;
        if (b.shape.x < b.shape.radius || b.shape.x > Gdx.graphics.getWidth() - b.shape.radius) {
            invert = invertX = true;
            c.hit = true;
        }
        if (b.shape.y < b.shape.radius || b.shape.y > Gdx.graphics.getHeight() - b.shape.radius) {
            invert = invertY = true;
            c.hit = true;
        }
        if (invert) {
            Movement m = movementComponents.get(e);
            if (invertX) m.direction.x = -m.direction.x;
            if (invertY) m.direction.y = -m.direction.y;
        }

    }

    private void collisionCheck(Entity e1, Entity e2, Collider collider) {
        final Circle b1 = bodyComponents.get(e1).shape;
        final Circle b2 = bodyComponents.get(e2).shape;
        final float dx = b1.x - b2.x;
        final float dy = b1.y - b2.y;
        final float dist = dx * dx + dy * dy;
        final float rSum = b1.radius + b2.radius;
        if (dist < rSum * rSum) {
            collider.hit = true;
            Vector2 dir = movementComponents.get(e1).direction;
            tmp.set(b1.x - b2.x, b1.y - b2.y);
            dir.set(tmp.nor());
        }
    }

    @Override
    protected void begin() {
        quadTree = new QuadTree<>(
                0,
                0,
                Gdx.graphics.getWidth(),
                Gdx.graphics.getHeight());
        getEntities().iterate(treeInit);
    }


    @Override
    protected void terminate() {
        quadTree = null;
    }
}
