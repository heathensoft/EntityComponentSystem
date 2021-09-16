package ecs.util.containers.exceptions;

public class EmptyCollectionException extends RuntimeException {

    public EmptyCollectionException(String note) {
        super (note);
    }
}
