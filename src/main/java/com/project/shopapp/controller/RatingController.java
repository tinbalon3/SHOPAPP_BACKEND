package com.project.shopapp.controller;

import com.project.shopapp.dto.RatingDTO;
import com.project.shopapp.exceptions.DataNotFoundException;
import com.project.shopapp.models.User;
import com.project.shopapp.response.ResponseObject;
import com.project.shopapp.response.rate.RatingResponse;
import com.project.shopapp.service.IRatingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
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
    public ResponseEntity<ResponseObject> getAllRating(
            @RequestParam(value = "user_id",required = false) Long userId,
            @RequestParam(value="product_id") Long productId
    ){
        List<RatingResponse> ratingRespons;
        if(userId == null){
            ratingRespons = iRatingService.getRatingByProductId(productId);
        }
        else {
            ratingRespons = iRatingService.getRatingByUserIdAndProductId(userId,productId);
        }

        return ResponseEntity.ok().body(ResponseObject.builder()
                        .data(ratingRespons)
                        .message("Lấy danh sách đánh giá thành công")
                        .status(HttpStatus.OK)
                .build());
    }

    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_USER')")
    @PutMapping("/{id}")
    public ResponseEntity<ResponseObject> updateRating(@PathVariable Long id, @Valid @RequestBody RatingDTO ratingDTO) throws DataNotFoundException {

            iRatingService.updateRating(id,ratingDTO);
            return ResponseEntity.ok(ResponseObject.builder()
                    .message("Update comment successfully")
                    .status(HttpStatus.OK)
                    .build());
    }

    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_USER')")
    @PostMapping("")
    public ResponseEntity<?> insertRating(@Valid @RequestBody RatingDTO ratingDTO) throws DataNotFoundException {

            User loginUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            if(loginUser.getId() != ratingDTO.getUserId()){
                return ResponseEntity.badRequest().body("Bạn không thể đánh giá khi khác user");
            }
            iRatingService.insertRating(ratingDTO);
            return ResponseEntity.ok().build();

    }
    @GetMapping("/stats/{productId}")
    public ResponseEntity<ResponseObject> getRatingStats(@PathVariable Long productId) throws Exception{

            List<RatingDTO> ratingDTOS = iRatingService.getStatRatingProduct(productId);
            return ResponseEntity.ok().body(ResponseObject.builder()
                            .status(HttpStatus.OK)
                            .data(ratingDTOS)
                            .message("Lấy đánh giá của sản phẩm thành công")
                    .build()
            );


    }

}
