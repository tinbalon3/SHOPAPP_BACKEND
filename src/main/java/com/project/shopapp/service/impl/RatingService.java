package com.project.shopapp.service.impl;

import com.project.shopapp.dto.RatingDTO;
import com.project.shopapp.exceptions.DataNotFoundException;
import com.project.shopapp.mapper.RatingMapper;
import com.project.shopapp.models.Rating;
import com.project.shopapp.models.Product;
import com.project.shopapp.models.User;
import com.project.shopapp.repositories.RatingRepository;
import com.project.shopapp.repositories.ProductRepository;
import com.project.shopapp.repositories.UserRepository;
import com.project.shopapp.response.rate.RatingResponse;
import com.project.shopapp.service.IRatingService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RatingService implements IRatingService {
    private final RatingRepository ratingRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    @Override
    @Transactional
    public Rating insertRating(RatingDTO rating) throws DataNotFoundException {
        User user = userRepository.findById(rating.getUserId()).orElseThrow(()-> new DataNotFoundException("User not found"));
        Product product = productRepository.findById(rating.getProductId()).orElseThrow(()-> new DataNotFoundException("product not found"));
        Rating newRating = RatingMapper.MAPPER.mapToRating(rating);
        newRating.setUser(user);
        newRating.setProduct(product);
        return ratingRepository.save(newRating);
    }

    @Override
    @Transactional
    public void deleteRating(Long ratingId) {
            ratingRepository.deleteById(ratingId);
    }

    @Override
    @Transactional
    public void updateRating(Long id, RatingDTO rating) throws DataNotFoundException {
        Rating existingRating = ratingRepository.findById(id)
                .orElseThrow(() -> new DataNotFoundException("Comment not found"));
        existingRating.setContent(rating.getContent());
        existingRating.setRating(rating.getRating());
        ratingRepository.save(existingRating);
    }

    @Override
    public List<RatingResponse> getRatingByUserIdAndProductId(Long userId, Long productId) {
        List<Rating> ratings = ratingRepository.findByUserIdAndProductId(userId,productId);
        return ratings.stream()
                .map(rating -> RatingResponse.fromRating(rating))
                .collect(Collectors.toList());
    }

    @Override
    public List<RatingResponse> getRatingByProductId(Long productId) {
        List<Rating> ratings = ratingRepository.findByProductId(productId);
        return ratings.stream()
                .map(rating -> RatingResponse.fromRating(rating))
                .collect(Collectors.toList());
    }

    @Override
    public List<RatingDTO> getStatRatingProduct(Long productID) {
        List<Rating> ratings = ratingRepository.findByProductId(productID);
        List<RatingDTO> ratingDTOS = ratings.stream().map(rating -> {
            RatingDTO ratingDTO = RatingMapper.MAPPER.mapToRatingDTO(rating);
            return ratingDTO;
        }).collect(Collectors.toList());
        return ratingDTOS;
    }
}
