package com.project.shopapp.response.rate;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RateResponse {

    private Long id;

    private String content;

    private int rating;

    @JsonProperty("created_at")
    private LocalDateTime createdAt;

}
