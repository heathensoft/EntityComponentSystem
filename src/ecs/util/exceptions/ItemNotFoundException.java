package ecs.util.exceptions;

public class ItemNotFoundException extends RuntimeException{

    public ItemNotFoundException(String note) {
        super(note);
    }
}
