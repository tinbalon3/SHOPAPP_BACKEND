package com.project.shopapp.response.product;

import com.project.shopapp.dto.ProductDTO;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ProductResponse {
    private String message;
    private ProductDTO product;
}
