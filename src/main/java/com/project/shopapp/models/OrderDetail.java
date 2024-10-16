package com.project.shopapp.models;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

@Table(name="order_details")
@Entity
@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name="order_id")
    @JsonIgnore
    private Order order;

    @ManyToOne
    @JoinColumn(name="product_id")

    private Product product;

    private Float price;

    @Column(name = "number_of_products",nullable = false)
    private int numberOfProduct;

    @Column(name = "total_money",nullable = false)
    private Float totalMoney;

    @ManyToOne
    @JoinColumn(name="coupon_id")
    @JsonBackReference
    private Coupon coupon;
}
