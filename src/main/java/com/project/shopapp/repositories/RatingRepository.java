package com.project.shopapp.repositories;

import com.project.shopapp.models.Rating;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RatingRepository extends JpaRepository<Rating,Long> {
    List<Rating> findByUserIdAndProductId(Long userId, Long productId);

    List<Rating> findByProductId(Long productId);

    Rating save(Rating rating);
}
