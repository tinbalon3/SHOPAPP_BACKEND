package com.project.shopapp.repositories;

import com.project.shopapp.dto.OrderDetailHistoryDTO;
import com.project.shopapp.models.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface OrderRepository extends JpaRepository<Order,Long> {
    List<Order> findByUserId(Long userId);
//    @Query("SELECT o FROM Order o WHERE o.active = true "
////            +
////            "AND (:keyword IS NULL OR :keyword = '' OR " +
////            "o.fullName LIKE %:keyword% OR " +
////            "o.shippingAddress.city LIKE %:keyword% OR " +
////            "o.shippingAddress.state LIKE %:keyword% OR " +
////            "o.note LIKE %:keyword% OR " +
////            "o.email LIKE %:keyword%)"
//    )
//    List<Order> findByKeyword(@Param("keyword") String keyword);

    @Query("SELECT new com.project.shopapp.dto.OrderDetailHistoryDTO( " +
            "od.id, " +
            "od.product.id, " +
            "od.price, " +
            "od.numberOfProduct, " +
            "od.totalMoney, " +
            "p.name, " +
            "p.thumbnail, " +
            "o.status, " +
            "o.orderDate ) " +
            "FROM User u " +
            "JOIN Order o ON u.id = o.user.id " +
            "JOIN OrderDetail od ON o.id = od.order.id " +
            "JOIN Product p ON od.product.id = p.id " +
            "WHERE u.id = :userId AND (:status IS NULL OR :status = '' OR o.status = :status)")
    Page<OrderDetailHistoryDTO> getOrderDetailHistory(@Param("status") String status,@Param("userId") Long userId, Pageable pageable);


}
