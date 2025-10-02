package com.xyz.booking.repository;

import com.xyz.booking.entity.Booking;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    // Check if a specific seat is already booked for a given show
    boolean existsByShow_IdAndSeatsContaining(Long showId, String seat);

    // Find all bookings for a given show
    List<Booking> findByShow_Id(Long showId);

    // Check if a show has any future bookings (used in Show delete/update rules)
    boolean existsByShow_IdAndShow_ShowTimeAfter(Long showId, LocalDateTime now);
}
