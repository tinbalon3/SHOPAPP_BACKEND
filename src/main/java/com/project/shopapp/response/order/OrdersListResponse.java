package com.project.shopapp.response.order;

import com.project.shopapp.dto.OrderResponseDTO;
import com.project.shopapp.dto.ProductDTO;
import com.project.shopapp.models.Order;
import lombok.*;

import java.util.List;

@Data
@Getter
@Setter
@AllArgsConstructor
@Builder
@NoArgsConstructor
public class OrdersListResponse {
    private List<OrderResponseDTO> orders;
    private Long totalElements;

}
