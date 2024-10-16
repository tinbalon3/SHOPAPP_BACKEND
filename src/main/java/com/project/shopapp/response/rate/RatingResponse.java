package com.project.shopapp.response.rate;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.project.shopapp.mapper.RatingMapper;
import com.project.shopapp.mapper.UserMapper;
import com.project.shopapp.models.Rating;
import com.project.shopapp.response.user.UserResponse;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RatingResponse {
    private RateResponse rating;
    @JsonProperty("user")
    private UserResponse userResponse;
    public static RatingResponse fromRating(Rating rating){
        return RatingResponse.builder()
                .rating(RatingMapper.MAPPER.mapToRatingResponse(rating))
                .userResponse(UserMapper.MAPPER.mapToUserResponse(rating.getUser()))
                .build();
    }
}
