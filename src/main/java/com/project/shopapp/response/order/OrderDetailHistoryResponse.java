package com.project.shopapp.response.order;

import com.project.shopapp.request.OrderDetailHistoryRequest;
import lombok.*;

import java.util.List;
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OrderDetailHistoryResponse {
    private List<OrderDetailHistoryRequest> orderDetails;
    private Long totalElements;
}
