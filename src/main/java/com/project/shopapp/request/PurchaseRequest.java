package com.project.shopapp.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.project.shopapp.dto.ItemPurchaseDTO;
import com.project.shopapp.dto.CustomerDTO;
import com.project.shopapp.models.Address;
import jakarta.validation.constraints.Min;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.Date;
import java.util.List;

@Getter
@Setter

public class PurchaseRequest implements Serializable {
    @JsonProperty("customer")
    private CustomerDTO customer;
    @JsonProperty("shipping_address")
    private Address shippingAddress;
    @JsonProperty("billing_address")
    private Address billingAddress;
    private String note;

    @JsonProperty("totalAmount")
    private Float totalMoney;
    @JsonProperty("shipping_method")
    private String shippingMethod;
    @JsonProperty("payment_method")
    private String paymentMethod;
    @JsonProperty("cart_items")
    private List<ItemPurchaseDTO> cartItems;
    @JsonProperty("reason")
    private String reason;

}
