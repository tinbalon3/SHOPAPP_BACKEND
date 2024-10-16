package com.project.shopapp.response.order;

import com.project.shopapp.dto.OrderDetailDTO;
import com.project.shopapp.models.OrderDetail;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OrderDetailResponse {
    private String message;
    private OrderDetailDTO orderDetail;
}
