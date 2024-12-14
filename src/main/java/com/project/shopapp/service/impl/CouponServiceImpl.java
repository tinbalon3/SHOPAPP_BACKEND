package com.project.shopapp.service.impl;

import com.project.shopapp.models.Coupon;
import com.project.shopapp.models.CouponCondition;
import com.project.shopapp.repositories.CouponConditionRepository;
import com.project.shopapp.repositories.CouponRepository;
import com.project.shopapp.service.ICouponService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;


import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CouponServiceImpl implements ICouponService {
    private final CouponRepository couponRepository;
    private final CouponConditionRepository couponConditionRepository;
    @Override
    public double calculateCouponValue(String couponCode, double totalAmount) {
        Coupon coupon = couponRepository.findByCode(couponCode)
                .orElseThrow(() -> new IllegalArgumentException("Mã giảm giá không hợp lệ."));
        if(!coupon.isActive()) {
            throw new IllegalArgumentException("Mã giảm giá đã hết hạn.");
        }
        double discount = calculateDiscount(coupon,totalAmount);
        if(discount == 0) {
            throw new IllegalArgumentException("Bạn không thõa điều kiện để được giảm giá.");
        }
        return discount;
    }

    @Override
    public void activeCoupon(Long couponId, boolean active) {
        Coupon coupon = couponRepository.findById(couponId)
                .orElseThrow(() -> new IllegalArgumentException("Mã giảm giá không hợp lệ."));
        coupon.setActive(false);
        couponRepository.save(coupon);

    }

    private double calculateDiscount(Coupon coupon, double totalAmount) {

        List<CouponCondition> conditions = couponConditionRepository.findByCouponId(coupon.getId());
        double discount = 0.0;

        for(CouponCondition condition : conditions){
            String attribute = condition.getAttribute();
            String operator = condition.getOperator();
            String value = condition.getValue();
            double percentDiscount = Double.valueOf(
                    String.valueOf(condition.getDiscountAmount()));
            if(attribute.equals("minimum_amount")){
                if(operator.equals(">") && totalAmount > Double.parseDouble(value)) {
                    discount += totalAmount * percentDiscount / 100;
                }
            } else if (attribute.equals("applicable_date")) {
                if(operator.equalsIgnoreCase("BETWEEN")) {
                    String[] dateBetween = value.split(",");
                    LocalDate startDate = LocalDate.parse(dateBetween[0]);
                    LocalDate endDate  = LocalDate.parse(dateBetween[1]);
                    LocalDate currentDate = LocalDate.now();
                    if(currentDate.isAfter(startDate) && currentDate.isBefore(endDate)) {
                        discount += totalAmount * percentDiscount / 100;
                    }
                }
                else if(operator.equalsIgnoreCase("=")) {
                    LocalDate currentDate = LocalDate.now();
                    LocalDate applicableDate = LocalDate.parse(value);
                    if(currentDate.isEqual(applicableDate)) {
                        discount += totalAmount * percentDiscount / 100;
                    }
                }


            }


        }
        return discount;
    }
}
