package com.project.shopapp.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import org.hibernate.validator.constraints.Length;


@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class RatingDTO {
    @JsonProperty("product_id")
    private Long productId;

    @JsonProperty("user_id")
    private Long userId;

    @Length(max = 50, message = "Nội dung đánh giá không được vượt quá 50 ký tự.")
    private String content;

    private int rating;
}
