package com.project.shopapp.repositories;

import com.project.shopapp.models.Rating;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
@Repository
public interface RatingRepository extends JpaRepository<Rating,Long> {
    List<Rating> findByUserIdAndProductId(Long userId, Long productId);

    List<Rating> findByProductId(Long productId);

    Rating save(Rating rating);
}
