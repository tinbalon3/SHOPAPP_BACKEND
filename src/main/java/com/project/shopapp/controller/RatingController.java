package com.project.shopapp.controller;

import com.project.shopapp.dto.RatingDTO;
import com.project.shopapp.models.User;
import com.project.shopapp.response.rate.RatingResponse;
import com.project.shopapp.service.IRatingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("${api.prefix}/rating")
@RequiredArgsConstructor
public class RatingController {
    private final IRatingService iRatingService;

    @GetMapping("")

    public ResponseEntity<List<RatingResponse>> getAllRating(
            @RequestParam(value = "user_id",required = false) Long userId,
            @RequestParam(value="product") Long productId
    ){
        List<RatingResponse> ratingRespons;
        if(userId == null){
            ratingRespons = iRatingService.getRatingByProductId(productId);
        }
        else {
            ratingRespons = iRatingService.getRatingByUserIdAndProductId(userId,productId);
        }

        return ResponseEntity.ok().body(ratingRespons);
    }

    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_USER')")
    @PutMapping("/{id}")
    public ResponseEntity<?> updateRating(@PathVariable Long id, @Valid @RequestBody RatingDTO ratingDTO){
        try{
            iRatingService.updateRating(id,ratingDTO);
            return ResponseEntity.ok("Update comment successfully");
        }catch (Exception e){
            return ResponseEntity.badRequest().body(e.getMessage());
        }

    }

    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_USER')")
    @PostMapping("")
    public ResponseEntity<?> insertRating(@Valid @RequestBody RatingDTO ratingDTO){
        try{
            User loginUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            if(loginUser.getId() != ratingDTO.getUserId()){
                return ResponseEntity.badRequest().body("You cannot comment as another user");
            }

            iRatingService.insertRating(ratingDTO);
            return ResponseEntity.ok().build();
        }catch (Exception e){
            return ResponseEntity.badRequest().body(e.getMessage());
        }

    }
}
