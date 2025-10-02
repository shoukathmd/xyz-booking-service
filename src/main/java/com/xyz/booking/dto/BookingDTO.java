package com.xyz.booking.dto;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookingDTO {
    private Long id;
    private Long showId;
    private String customerName;
    private LocalDateTime bookingTime;
    private List<String> seats;
    private String status;
}
