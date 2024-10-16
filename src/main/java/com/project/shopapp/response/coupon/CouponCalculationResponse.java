package com.project.shopapp.response.coupon;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CouponCalculationResponse {
    @JsonProperty("result")
    private Double result;

    @JsonProperty("errorMessage")
    private String errorMessage;
}
