package com.project.shopapp.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.project.shopapp.models.Product;
import jakarta.persistence.Column;
import lombok.*;

import java.time.LocalDateTime;
import java.util.Date;

@Data
@Getter
@Setter
@NoArgsConstructor


public class OrderDetailHistoryDTO {
    private Long id;


    @JsonProperty("product_id")
    private Long productId;

    private Float price;

    @JsonProperty( "number_of_products")
    private int numberOfProduct;

    @JsonProperty( "total_money")
    private Float totalMoney;

    private String status;

    private String thumbnail;

    private String name;
    private Date orderDate;
    public OrderDetailHistoryDTO(Long id, Long productId, Float price, int numberOfProduct, Float totalMoney,String name,String thumbnail, String status,Date orderDate ) {
        this.id = id;
        this.productId = productId;
        this.price = price;
        this.numberOfProduct = numberOfProduct;
        this.totalMoney = totalMoney;
        this.status = status;
        this.thumbnail = thumbnail;
        this.name = name;
        this.orderDate = orderDate;
    }


}
