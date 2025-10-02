package com.xyz.booking.exception;

public class ShowHasFutureBookingsException extends RuntimeException {
    public ShowHasFutureBookingsException(Long id) {
        super("Cannot modify or delete show " + id + " as it has future bookings");
    }
}
