package com.nudge.ecs.util.containers.exceptions;

/**
 * KeyStateConflictException is an exception to indicate that an Array / Item provides some key / id with
 * a value that is NOT identified when it SHOULD BE. Extends RuntimeException
 *
 * @author Frederik Dahl
 * 24/08/2021
 */


public class KeyStateConflictException extends RuntimeException{
    
    public KeyStateConflictException(String message) {super(message);}
}
