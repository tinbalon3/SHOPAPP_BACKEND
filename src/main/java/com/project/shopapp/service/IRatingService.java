package com.project.shopapp.service;

import com.project.shopapp.dto.RatingDTO;
import com.project.shopapp.exceptions.DataNotFoundException;
import com.project.shopapp.models.Rating;
import com.project.shopapp.response.rate.RatingResponse;

import java.util.List;

public interface IRatingService {
    Rating insertRating(RatingDTO rating) throws DataNotFoundException;
    void deleteRating(Long ratingId);
    void updateRating(Long id, RatingDTO rating) throws DataNotFoundException;
    List<RatingResponse> getRatingByUserIdAndProductId(Long userId, Long productId);
    List<RatingResponse> getRatingByProductId(Long productId);

    List<RatingDTO> getStatRatingProduct(Long productID);
}
