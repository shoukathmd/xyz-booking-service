package com.xyz.booking.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShowDTO {
    private Long showId;
    private String movieTitle;
    private String language;
    private String genre;
    private String theatreName;
    private String cityName;
    private LocalDateTime showTime;
}
