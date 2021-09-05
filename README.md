
## Entity Component System

This is a module for a game engine I am working on. The module is still under development.


There are more than a few ways to design an ECS. And being written in Java, you can't really
manage caching / memory like you could do with languages like C or C++.
Where you could keep components tightly stacked next to each other i memory, to increase performance.

Even without this control, it still makes creating elements for interactive applications more manageable.
Instead of inheritance you have "entities" with components.
.ie if you want your entity to be able to fly, you might give it the flying component.
The flying component could have a layout like this:


public class Flying implements Component

    int altitude;
    float verticalForce;


When adding a component, the entity (basically an id) will be added to systems that are interested
in flying components. Components are data and systems process data.
.ie a rendering system might need a Sprite component and a Transform component.
Any entity meeting those requirements will be added to the system.
It does not matter what abstractions you impose on an entity. If you add a Flying component to
a Fish or a UI-button it has the potential to fly. And you can do this at runtime.
So a Fish is just a collection of properties, if you remove some components and add some other
that same entity is a trebuchet. You get the idea. It's modularity, not inheritance.
So that's an ECS in a nutshell.

Core design principles:

* Components have no functionality
* Systems have no state
* Entities are essentially id's




### references and inspiration:


[Wikipedia](https://en.wikipedia.org/wiki/Entity_component_system)

[A SIMPLE ENTITY COMPONENT SYSTEM (ECS) [C++]](https://austinmorlan.com/posts/entity_component_system/)

[Artemis ECS java](https://github.com/gemserk/artemis)

[Content Fueled Gameplay Programming in Frostpunk](https://www.youtube.com/watch?v=9rOtJCUDjtQ&t=2204s&ab_channel=GDC)

[Entity Component System #1](https://www.youtube.com/watch?v=5KugyHKsXLQ&ab_channel=RezBot)

[Entity Component System #2](https://www.youtube.com/watch?v=sOG4M-T__tQ&ab_channel=RezBot)

[CppCon 2014: Mike Acton "Data-Oriented Design and C++"](https://www.youtube.com/watch?v=rX0ItVEVjHc&t=4276s&ab_channel=CppCon)


