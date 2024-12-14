package com.project.shopapp.repositories;

import com.project.shopapp.dto.OrderHistoryDTO;
import com.project.shopapp.models.Order;
import com.project.shopapp.models.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
@Repository
public interface OrderRepository extends JpaRepository<Order,Long> {
    @Query("SELECT o.id, o.orderDate, o.status, o.paymentMethod, o.totalMoney, SUM(od.numberOfProduct) " +
            "FROM Order o " +
            "JOIN OrderDetail od ON o.id = od.order.id " +
            "WHERE (:status IS NULL OR :status = '' OR o.status = :status) AND o.user.id = :userId " +
            "GROUP BY o.id")
    List<Object[]> getOrderHistory(@Param("status") OrderStatus status,
                                          @Param("userId") Long userId,
                                          Pageable pageable);



}
