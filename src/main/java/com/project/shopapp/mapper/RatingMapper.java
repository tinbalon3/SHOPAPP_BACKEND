package com.project.shopapp.mapper;

import com.project.shopapp.dto.RatingDTO;
import com.project.shopapp.models.Rating;
import com.project.shopapp.response.rate.RateResponse;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;
@Mapper
public interface RatingMapper {
    RatingMapper MAPPER = Mappers.getMapper(RatingMapper.class);

    Rating mapToRating(RatingDTO ratingDTO);

    RateResponse mapToRatingResponse(Rating rating);

    RatingDTO mapToRatingDTO(Rating rating);

}
