package com.nudge.gdx.ecs.systems;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Circle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.nudge.ecs.ECS;
import com.nudge.ecs.ECSystem;
import com.nudge.ecs.Entity;
import com.nudge.ecs.Getter;
import com.nudge.ecs.util.containers.Iterator;
import com.nudge.gdx.ecs.components.Collider;
import com.nudge.gdx.ecs.components.Body;
import com.nudge.gdx.ecs.components.Movement;
import com.nudge.gdx.ecs.util.Point;
import com.nudge.gdx.ecs.util.QuadTree;

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
        quadTree.query(queried,collider.range);
        for (Entity o: queried)
            if (!o.equals(e))
                collisionCheck(e,o);
    }

    private void collisionCheck(Entity e1, Entity e2) {
        final Circle b1 = bodyComponents.get(e1).shape;
        final Circle b2 = bodyComponents.get(e2).shape;
        final float dx = b1.x - b2.x;
        final float dy = b1.y - b2.y;
        final float dist = dx * dx + dy * dy;
        final float rSum = b1.radius + b2.radius;
        if (dist < rSum * rSum) {
            colliderComponents.get(e1).hit = true;
            colliderComponents.get(e2).hit = true;
            Vector2 dir1 = movementComponents.get(e1).direction;
            Vector2 dir2 = movementComponents.get(e2).direction;
            tmp.set(b1.x - b2.x, b1.y - b2.y);
            dir1.set(tmp.nor());
            tmp.set(b2.x - b1.x, b2.y - b1.y);
            dir2.set(tmp.nor());
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
    protected void end() {
        queried.clear();
    }
}
