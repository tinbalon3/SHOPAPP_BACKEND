package com.project.shopapp.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.project.shopapp.models.OrderStatus;
import com.project.shopapp.models.Product;
import jakarta.persistence.Column;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Date;

@Data
@Getter
@Setter

@Builder

public class OrderHistoryDTO {

    private Long id;

    @JsonProperty( "totalProducts")
    private Long totalProducts;

    @JsonProperty("total_money")
    private Float totalMoney;


    private OrderStatus status;

    private LocalDateTime orderDate;

    private String paymentMethod;



}
