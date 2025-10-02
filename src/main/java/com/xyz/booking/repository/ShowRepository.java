package com.xyz.booking.repository;

import com.xyz.booking.entity.Show;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface ShowRepository extends JpaRepository<Show, Long> {

    List<Show> findByMovie_TitleIgnoreCaseAndTheatre_City_NameIgnoreCaseAndShowTimeBetween(
            String movieTitle,
            String cityName,
            LocalDateTime startOfDay,
            LocalDateTime endOfDay
    );
}
