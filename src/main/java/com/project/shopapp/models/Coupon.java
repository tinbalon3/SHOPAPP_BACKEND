package com.project.shopapp.models;

import jakarta.persistence.*;
import lombok.*;

@Table(name="coupons")
@Entity
@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Coupon {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "code",nullable = false,unique = true)
    private String code;

    @Column(name = "active", nullable = false)
    private boolean active;
}
