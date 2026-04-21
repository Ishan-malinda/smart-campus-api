package com.smartcampus.exception;

public class LinkedResourceNotFoundException extends RuntimeException {
    public LinkedResourceNotFoundException(String roomId) {
        super("Referenced Room ID '" + roomId + "' does not exist in the system.");
    }
}
