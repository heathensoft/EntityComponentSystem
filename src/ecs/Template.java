package ecs;

import ecs.util.Container;

/**
 *
 * A container for Components.
 * Used to design an entity .ie Car, Knight, Button.
 *
 * @author Frederik Dahl
 * 20/08/2021
 */


public abstract class Template {

    public Container<Component> components;
}
