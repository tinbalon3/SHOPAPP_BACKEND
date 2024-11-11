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
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy coupon."));
        if(!coupon.isActive()) {
            throw new IllegalArgumentException("Coupon không còn hiệu lực.");
        }
        double discount = calculateDiscount(coupon,totalAmount);
        double finalAmount = totalAmount - discount;
        return finalAmount;
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
                LocalDate applicableDate = LocalDate.parse(value);
                LocalDate currentDate = LocalDate.now();
                if(operator.equalsIgnoreCase("BETWEEN")
                && currentDate.isEqual(applicableDate)) {
                    discount += totalAmount * percentDiscount / 100;
                }
            }

        }
        return discount;
    }
}
