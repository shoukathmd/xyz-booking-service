package com.xyz.booking.dto.request;

import lombok.*;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShowRequestDTO {
    private Long movieId;
    private Long theatreId;
    private LocalDateTime showTime;
}

