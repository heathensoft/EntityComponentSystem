package com.nudge.ecs.gdx.systems;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Circle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.nudge.ecs.ECS;
import com.nudge.ecs.ECSystem;
import com.nudge.ecs.Entity;
import com.nudge.ecs.Getter;
import com.nudge.ecs.gdx.components.Body;
import com.nudge.ecs.gdx.components.Collider;
import com.nudge.ecs.gdx.components.Dying;
import com.nudge.ecs.gdx.components.Velocity;
import com.nudge.ecs.gdx.util.Point;
import com.nudge.ecs.gdx.util.QuadTree;
import com.nudge.ecs.util.containers.Iterator;

/**
 * @author Frederik Dahl
 * 20/09/2021
 */


public class CollisionSystem extends ECSystem {

    private QuadTree<Entity> quadTree;
    private final Array<Entity> queried;
    private final Getter<Body> bodyComponents;
    private final Getter<Velocity> velocityComponents;
    private final Circle collisionRange = new Circle();
    private final Vector2 tmp = new Vector2();

    private final Iterator<Entity> quadTreeInsert = new Iterator<>() {
        @Override
        public void next(Entity e) {
            Body b = bodyComponents.getUnsafe(e);
            quadTree.insert(new Point<>(
                    b.position.x,
                    b.position.y,
                    b.radius,
                    e
            ));
        }
    };

    public CollisionSystem(ECS ecs, int cap){
        super(ecs,cap, ecs.getGroup(Body.class, Collider.class, Velocity.class));
        velocityComponents = ecs.getter(Velocity.class);
        bodyComponents = ecs.getter(Body.class);
        queried = new Array<>(cap);
    }

    @Override
    protected void processEntity(Entity e) {

        Body b1 = bodyComponents.getUnsafe(e);
        if (!offScreen(e,b1)) {
            collisionRange.set(
                    b1.position.x,
                    b1.position.y,
                    b1.radius
            );
            quadTree.query(
                    queried,
                    collisionRange
            );
            for (Entity o: queried){

                if (!e.equals(o)) {

                    Body b2 = bodyComponents.getUnsafe(o);
                    final float eX = b1.position.x;
                    final float oX = b2.position.x;
                    final float eY = b1.position.y;
                    final float oY = b2.position.y;
                    final float dx = eX - oX;
                    final float dy = eY - oY;
                    final float dist = dx * dx + dy * dy;
                    final float rSum = b1.radius + b2.radius;

                    if (dist < rSum * rSum) { // if we have an actual collision;
                        Velocity v = velocityComponents.getUnsafe(e);
                        tmp.set(eX - oX, eY - oY);
                        tmp.nor().scl(v.speed);
                        v.velocity.set(tmp);

                        if (!b1.infected) {
                            if (b1.vulnerable && b2.infected) {
                                b1.infected = true;
                                b1.color = Body.RED;
                                getEcs().entityManager().addComponent(e,new Dying());
                            }
                        }
                    }
                }
            }
            queried.clear();
        }
    }

    private boolean offScreen(Entity e, Body b) {

        final float x = b.position.x;
        final float y = b.position.y;
        final float r = b.radius;

        boolean offScreen = false;

        if (x < 0) {
            b.position.x = x + Gdx.graphics.getWidth();
            offScreen = true;
        }
        else if (x > Gdx.graphics.getWidth()) {
            b.position.x = x - Gdx.graphics.getWidth();
            offScreen = true;
        }
        else if (y < 0) {
            b.position.y = y + Gdx.graphics.getHeight();
            offScreen = true;
        }
        else if (y > Gdx.graphics.getHeight()) {
            b.position.y = y - Gdx.graphics.getHeight();
            offScreen = true;
        }

        /*
        if (x < r || x > Gdx.graphics.getWidth() - r) {
            if (x<-10) System.out.println("fffff");
            Velocity v = velocityComponents.getUnsafe(e);
            v.velocity.scl(-1,1);
            collision = true;
        }
        if (y < r || y > Gdx.graphics.getHeight() - r) {
            Velocity v = velocityComponents.getUnsafe(e);
            v.velocity.scl(1,-1);
            collision = true;
        }
         */

        return offScreen;
    }

    @Override
    protected void begin() {
        quadTree = new QuadTree<>(
                0,
                0,
                Gdx.graphics.getWidth(),
                Gdx.graphics.getHeight());
        getEntities().iterate(quadTreeInsert);
    }

    @Override
    protected void terminate() {
        quadTree = null;
    }
}
