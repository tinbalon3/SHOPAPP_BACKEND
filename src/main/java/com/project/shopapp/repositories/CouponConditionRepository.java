package com.project.shopapp.repositories;

import com.project.shopapp.models.Coupon;
import com.project.shopapp.models.CouponCondition;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
@Repository
public interface CouponConditionRepository extends JpaRepository<CouponCondition, Long> {
    List<CouponCondition> findByCouponId(long couponId);
}
