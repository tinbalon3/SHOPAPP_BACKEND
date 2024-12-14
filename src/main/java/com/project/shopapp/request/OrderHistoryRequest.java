package com.project.shopapp.request;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.project.shopapp.models.Order;
import com.project.shopapp.models.Product;
import jakarta.persistence.Column;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.*;

import java.util.Date;

@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderHistoryRequest {

    private Long id;

    private Float price;

    @JsonProperty( "number_of_products")
    private int numberOfProduct;

    @JsonProperty("total_money")
    private Float totalMoney;

    private String status;

    private String thumbnail;

    private String name;

    private Date orderDate;

}
