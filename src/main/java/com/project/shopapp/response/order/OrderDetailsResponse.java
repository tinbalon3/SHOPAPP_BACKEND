package com.project.shopapp.response.order;

import com.project.shopapp.dto.OrderDetailDTO;
import com.project.shopapp.models.OrderDetail;
import lombok.*;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OrderDetailsResponse {
    private String message;
    private List<OrderDetailDTO> orderDetails;
}
