package com.project.shopapp.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.List;

@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ProductDetailDTO {

    private Long id;

    private String name;

    private float price;

    private int stock;

    private String thumbnail;

    private String description;

    @JsonProperty("category_id")
    private long categoryId;

    private List<ProductImageDTO> product_images;

    @JsonProperty("number_of_rating")
    private Long numberOfRating;

    @JsonProperty("sum_of_rating")
    private Long sumOfRating;

}
