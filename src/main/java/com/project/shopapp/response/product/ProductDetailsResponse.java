package com.project.shopapp.response.product;

import com.project.shopapp.dto.ProductDetailDTO;
import lombok.*;

@Data
@Getter
@Setter
@AllArgsConstructor
@Builder
@NoArgsConstructor
public class ProductDetailsResponse {
    private String message;
    private ProductDetailDTO productDetailDTO;
}
