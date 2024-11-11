package com.project.shopapp.dto;

import com.project.shopapp.models.Order;
import com.project.shopapp.request.PurchaseRequest;
import lombok.*;

import java.io.Serializable;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PaymentObject implements Serializable {
    private Order order;
    private PurchaseRequest purchaseRequest;
}
