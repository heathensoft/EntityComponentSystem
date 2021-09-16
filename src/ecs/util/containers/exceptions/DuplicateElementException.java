package ecs.util.containers.exceptions;

public class DuplicateElementException extends RuntimeException{

    public DuplicateElementException(String note) {
        super(note);
    }
}
