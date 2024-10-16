package com.project.shopapp.response.product;

import com.project.shopapp.dto.ProductDTO;
import lombok.*;

import java.util.List;

@Data
@Getter
@Setter
@AllArgsConstructor
@Builder
@NoArgsConstructor
public class ProductListResponse {
    private List<ProductDTO> products;
    private Long totalElements;
    private int totalPages;
}
