package com.project.shopapp.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.project.shopapp.models.Address;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.util.Date;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class OrderDTO {
    private Long id;
    @JsonProperty("user_id")
    @Min(value=1,message = "User's ID must be > 0")
    private Long user_id;
    @JsonProperty("fullname")
    private String fullName;
    private String email;
    @JsonProperty("phone_number")
    @Min(value=5,message = "Phone number must be at least 5 characters")
    @NotBlank(message = "Phone number is required")
    private String phone_number;
    private String note;

    private String status;

    @Min(value=0,message = "Total money must be >= 0")
    @JsonProperty("total_money")
    private Float totalMoney;

    @JsonProperty("shipping_method")
    private String shippingMethod;

    @JsonProperty("shipping_address")
    private Address shippingAddress;
    @JsonProperty("billing_address")
    private Address billingAddress;
    @JsonProperty("shipping_date")
    private Date shippingDate;

    @JsonProperty("payment_method")
    private String paymentMethod;
}
