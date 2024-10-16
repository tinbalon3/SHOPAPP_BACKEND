package com.project.shopapp.response.order;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.project.shopapp.models.Order;
import com.project.shopapp.models.User;
import lombok.*;

import java.time.LocalDate;
import java.util.Date;


@Getter
@Setter
@AllArgsConstructor
@Builder
@NoArgsConstructor
public class OrderResponse {
   private String message;
   private Order order;
}
