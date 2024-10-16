package com.project.shopapp.models;

import jakarta.persistence.*;
import lombok.*;

@Table(name="product_images")
@Entity
@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProductImage {

    public final static  int MAXIMUM_IMAGES_PER_PRODUCT = 5;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name="product_id")
    private Product product;

    @Column(name = "images_url",length = 300)
    private String imageUrl;
}
