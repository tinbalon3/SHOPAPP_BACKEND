package com.project.shopapp.response.order;

import com.project.shopapp.dto.OrderHistoryDTO;
import com.project.shopapp.request.OrderHistoryRequest;
import lombok.*;

import java.util.List;
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OrderHistoryResponse {
    private List<OrderHistoryDTO> orderDetails;
    private Long totalElements;
}
