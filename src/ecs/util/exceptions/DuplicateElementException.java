package ecs.util.exceptions;

public class DuplicateElementException extends RuntimeException{

    public DuplicateElementException(String note) {
        super(note);
    }
}
