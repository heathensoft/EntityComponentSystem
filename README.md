
## Entity Component System (NudgeECS)


### Status
20 sep 2021: Functioning. Under development.

### Introduction

The ECS module is a game object management system,
favoring composition over inheritance.
Separating entities, components and systems.

The module is open-source and will be a core module in the 
<a href="https://github.com/fre-dahl/Nudge">Nudge</a> framework.
But for now we have a working example in Libgdx.

Screenshot from the <a href="https://github.com/fre-dahl/EntityComponentSystem/tree/main/src/com/nudge/ecs/gdx">LibGDX example</a>.

(Starting with 30 000 colliding bodies. Introducing 2 "infected")
![1](https://github.com/fre-dahl/EntityComponentSystem/blob/main/screenshots/screenshot2.png?raw=true)


There are more than a few ways to design an ECS. This being written in Java, you can't really
manage caching / memory in the same way you could do with languages like C or C++.

Even without this control, it still makes creating elements for interactive applications more manageable.
In short, instead of inheritance you have "entities" with a set of components.
Making it easier to create and change objects, by just adding or removing components.

Let's say you start an inheritance tree structure with "Animal" being the absolute super.
Somewhere down the tree you make a separation between something like flying and non-flying,
or maybe you call them Avians and Mammals. You are kinda stuck with your choice of abstraction.
They are now separated. You might have spent the afternoon trying figure out
how the inheritance structure should be set up. If you eventually want a specific land-animal
having some bird like quality from a subset of the Avians.
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
But I still wanted to try the "Structure of Arrays" approach for science.
Either way, both implementations are valid.


#### What are components?
Components are just containers of data. They have no functionality. In this ECS,
the base class Component is just an empty Interface.


public class Transform implements Component

    Vector2 scale;
    Vector3 position;
    float rotation;


#### What are systems?

Systems are the processors of components. This is where the logic is implemented.
One system might operate on position and velocity (write).
Another might operate on position and texture (read).

    
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

(As it spreads, infected entities give a "Dying" component to vulnerable entities.
the system then adds them to be processed by the Dying-system)
![1](https://github.com/fre-dahl/EntityComponentSystem/blob/main/screenshots/screenshot3.png?raw=true)



### Recommendations, refs. and inspiration:


[Wikipedia](https://en.wikipedia.org/wiki/Entity_component_system)

[They're Not Just For Game Developers](https://www.youtube.com/watch?v=SFKR5rZBu-8&t=1249s&ab_channel=MarsButtfield-Addison)

[A SIMPLE ENTITY COMPONENT SYSTEM (ECS) [C++]](https://austinmorlan.com/posts/entity_component_system/)

[Artemis ECS java](https://github.com/gemserk/artemis)

[Content Fueled Gameplay Programming in Frostpunk](https://www.youtube.com/watch?v=9rOtJCUDjtQ&t=2204s&ab_channel=GDC)

[Entity Component System #1](https://www.youtube.com/watch?v=5KugyHKsXLQ&ab_channel=RezBot)

[Entity Component System #2](https://www.youtube.com/watch?v=sOG4M-T__tQ&ab_channel=RezBot)

[CppCon 2014: Mike Acton "Data-Oriented Design and C++"](https://www.youtube.com/watch?v=rX0ItVEVjHc&t=4276s&ab_channel=CppCon)


