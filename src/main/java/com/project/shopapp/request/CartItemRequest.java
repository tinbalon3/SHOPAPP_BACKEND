package com.project.shopapp.request;

import lombok.*;

@AllArgsConstructor
@Data
@Getter
@Setter
@NoArgsConstructor
public class CartItemRequest {
    private Long id;
    private String name;
    private String thumbnail;
    private Float price;
    private int quantity;
    private int number_of_rating;
    private int sum_of_rating;
    private int stock;
}
