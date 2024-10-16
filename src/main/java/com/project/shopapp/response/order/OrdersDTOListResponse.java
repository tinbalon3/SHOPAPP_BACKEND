package com.project.shopapp.response.order;

import com.project.shopapp.dto.OrderDTO;
import lombok.*;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@Builder
@NoArgsConstructor
public class OrdersDTOListResponse {
    private Long totalElements;
    private Long totalPages;
    private List<OrderDTO> orders;
}
