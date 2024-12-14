package com.project.shopapp.models;


import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.*;

import net.minidev.json.annotate.JsonIgnore;
import org.hibernate.Transaction;
import org.hibernate.annotations.CreationTimestamp;


import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


@Table(name="orders")
@Entity
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor

public class Order  implements Serializable{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name="user_id",referencedColumnName = "id")
    private User user;

    @Column(name = "fullname",length = 100)
    private String fullName;

    @Column(name = "email",length = 100)
    private String email;

    @Column(name = "phone_number",nullable = false,length = 100)
    private String phone_number;

    @Column(name = "note",length = 100)
    private String note;

    @CreationTimestamp
    @Column(name = "order_date")
    private Date orderDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private OrderStatus status;

    @Column(name = "total_money")
    private Float totalMoney;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "shipping_address_id",referencedColumnName = "id")
    private Address shippingAddress;

//    @OneToOne(cascade = CascadeType.ALL)
//    @JoinColumn(name = "billing_address_id",referencedColumnName = "id")
//    private Address billingAddress;


    @Column(name = "shipping_date")
    private LocalDate shippingDate;

    @Column(name = "tracking_number")
    private String trackingNumber;

    @Column(name = "payment_method")
    private String paymentMethod;

    @Column(name = "shipping_method")
    private String shippingMethod;

    @Column(name = "active")
    private boolean active;

    @ManyToOne
    @JoinColumn(name="coupon_id")
    @JsonBackReference("order-coupon")
    private Coupon coupon;

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonBackReference("order-transaction")
    @JoinColumn(name = "transaction_id", referencedColumnName = "id")
    private Transactions transaction;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonBackReference("order-order_detail")
    private List<OrderDetail> orderDetails = new ArrayList<>();

}

