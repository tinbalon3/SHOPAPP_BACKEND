package com.project.shopapp.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import jakarta.persistence.JoinColumn;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Setter
@Getter
public class ProductImageDTO {
    private Long id;
    @JsonProperty("product_id")
    private Long productId;
    @JsonProperty("images_url")
    @Size(min=5,max=200,message = "Images's name")
    private String imageUrl;


}
