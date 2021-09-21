
## Entity Component System (NudgeECS)


### Status
20 sep 2021: Functioning. Under development.

### Introduction

The ECS module is a game object management system,
favoring composition over inheritance.
Separating logic and data.

The stand-alone ECS module is open-source and can be used with any framework.
To show you how it could be used, the repo. includes a working test-example for the <a href="https://libgdx.com/">LibGDX</a> framework
in the src folder.

There will be a java-doc up when it's complete. Some core parts has doc-comments in the code.

Screenshot from the LibGDX <a href="https://github.com/fre-dahl/EntityComponentSystem/tree/main/src/com/nudge/ecs/gdx">example</a>. 


![1](https://github.com/fre-dahl/EntityComponentSystem/blob/main/screenshots/screenshot2.png?raw=true)


There are more than a few ways to design an <a href="https://en.wikipedia.org/wiki/Entity_component_system">ECS</a>.
With programming languages like C or C++ you could keep the component data stacked next to each other in memory to reduce 
the number of cache-misses when looping through them in the systems. But this being written in Java, you can't really
manage caching / memory-layout in the same way.

The main point of an ECS though, is how you organize your game-objects.
instead of inheritance you have "entities" with a set of components. 
Making it easier to create and change objects. Adding or removing components change their behavior at run-time.

Let's say you start an inheritance tree structure with "Animal" being the absolute super.
Somewhere down the tree you make a separation between something like flying and non-flying,
or maybe you call them Avians and Mammals. You are kinda stuck with your choice of abstraction.
They are now separated. You might have spent the afternoon trying figure out
how the inheritance structure should be set up for your application. If you eventually want a specific land-animal
to have some bird like quality from a subset of the Avians.
You would need to rethink your abstraction. Maybe just reuse the code from the avian-subset.
Maybe the choice of abstraction wasn't good enough in the first place,
or maybe you could just have it implement an interface to represent the wanted ability.
Maybe the subset of birds is better off just implementing that interface as well.
That's logical. What about invertebrates? Flying fish? 

It would be nice not to think about that. Anything can fly if you just gave it the ability.
You could, at run-time alter the same entity from a menu-button to a trebuchet.

### The implementation
In some ECS implementations the entity would be the container for its components. But for
this ECS the entities are separated from its components altogether. Keeping instead the components
together in arrays. The entity then, would be the key to look them up.

In Java, having the entities be containers for its components might be a better idea. Since,
again you have no real control over where the components are stored in memory.
But I still wanted to try the "Structure of Arrays" approach.
Either way, both implementations work.


#### What are components?
Components are just containers of data. They have no functionality. In this ECS,
the base class Component is just an empty Interface.


public class Transform implements Component

    Vector2 scale;
    Vector2 position;
    float rotation;


#### What are systems?

Systems are the "processors" of components. This is where the logic is implemented.
If an entity is registered to have the required components of a system, it will be processed by that system.
One system could operate on position and velocity. Another might operate on position and texture.

    
    entities.iterate(process(e))

    @Override
    process(Entity e) {
        Sprite sprite = spriteComponents.get(e);
        Transform transform = transformComponents.get(e);
        batch.draw(sprite,transform)
    }

When enabling/disabling/deleting an entity or adding/removing components from it.
The entity gets revalidated by each system. Gets kept/removed or added dependant
of the specific system requirements.

Modularity, not inheritance.

Core design principles:

* Components have no functionality
* Systems have no state
* Entities are essentially id's


### LibGDX usage example

We are simulating the spreading of a virus:

![1](https://github.com/fre-dahl/EntityComponentSystem/blob/main/screenshots/screenshot3.png?raw=true)

So we need some entity components:

* A body with a shape, color and position. 
* A velocity with a direction and speed.
* A collider to indicate that it can collide.
* A "dying" component to give it that ability and a time and a die.

For the body we have some additional data for convenience.
It could instead be delegated to another component:
The body component also stores a boolean for whether it's vulnerable.
And a boolean for whether it is infected.

The core of it is this:

If an infected body collides with a vulnerable non-infected,
The vulnerable is now infected and is assigned a Dying-component.
(You could also be infected and not be vulnerable)

The systems we need then are (Required components):
* A renderer to render the body shape and color (body)
* A collision-System to manage the collisions (collider, body, velocity)
* A movement-system so it can move (velocity, body)
* A dying system to reduce the time to die, and delete the entity (dying)


(Here with 30k vulnerable bodies, 2 seconds to die on infection)

![1](https://github.com/fre-dahl/EntityComponentSystem/blob/main/screenshots/animation.gif?raw=true)



### Recommendations, refs. and inspiration:


[Wikipedia](https://en.wikipedia.org/wiki/Entity_component_system)

[They're Not Just For Game Developers](https://www.youtube.com/watch?v=SFKR5rZBu-8&t=1249s&ab_channel=MarsButtfield-Addison)

[A SIMPLE ENTITY COMPONENT SYSTEM (ECS) [C++]](https://austinmorlan.com/posts/entity_component_system/)

[Artemis ECS java](https://github.com/gemserk/artemis)

[Content Fueled Gameplay Programming in Frostpunk](https://www.youtube.com/watch?v=9rOtJCUDjtQ&t=2204s&ab_channel=GDC)

[Entity Component System #1](https://www.youtube.com/watch?v=5KugyHKsXLQ&ab_channel=RezBot)

[Entity Component System #2](https://www.youtube.com/watch?v=sOG4M-T__tQ&ab_channel=RezBot)

[CppCon 2014: Mike Acton "Data-Oriented Design and C++"](https://www.youtube.com/watch?v=rX0ItVEVjHc&t=4276s&ab_channel=CppCon)


