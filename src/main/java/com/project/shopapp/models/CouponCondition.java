package com.project.shopapp.models;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Table(name="coupon_conditions")
@Entity
@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CouponCondition {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="coupon_id",nullable = false)
    @JsonBackReference
    private Coupon coupon;

    @Column(name = "attribute",nullable = false)
    private String attribute;

    @Column(name = "operator",nullable = false)
    private String operator;

    @Column(name="value",nullable = false)
    private String value;

    @Column(name = "discount_amount",nullable = false)
    private BigDecimal discountAmount;

}
