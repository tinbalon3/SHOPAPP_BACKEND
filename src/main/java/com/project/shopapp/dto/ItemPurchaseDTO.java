package com.project.shopapp.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;


@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ItemPurchaseDTO {
    @JsonProperty("product_id")
    private Long id;
    private int quantity;
}
