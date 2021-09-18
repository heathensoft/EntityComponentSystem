package com.nudge.ecs.util.containers.exceptions;

public class ItemNotFoundException extends RuntimeException{

    public ItemNotFoundException(String note) {
        super(note);
    }
}
