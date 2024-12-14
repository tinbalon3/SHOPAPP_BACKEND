package com.project.shopapp.repositories;

import com.project.shopapp.models.OrderDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
@Repository
public interface OrderDetailRepository extends JpaRepository<OrderDetail,Long> {
    List<OrderDetail> findByOrderId(Long orderId);
    @Query("SELECT od.product.id FROM OrderDetail od WHERE od.order.id = :orderId")
    List<Long> findProductIdsByOrderId(@Param("orderId") Long orderId);

}
