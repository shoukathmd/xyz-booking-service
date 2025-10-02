package com.xyz.booking.exception;

public class ShowNotFoundException extends RuntimeException {
    public ShowNotFoundException(Long id) {
        super("Show with id " + id + " not found");
    }
}
