package ecs.util.exceptions;

public class EmptyCollectionException extends RuntimeException {

    public EmptyCollectionException(String note) {
        super (note);
    }
}
