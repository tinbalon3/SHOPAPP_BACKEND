package com.project.shopapp.response.product;

import lombok.*;

@Data
@Getter
@Setter
@AllArgsConstructor
@Builder
@NoArgsConstructor
public class ProductMaxAndMinPriceResponse {
    private Double maxPrice;
    private Double minPrice;
}
