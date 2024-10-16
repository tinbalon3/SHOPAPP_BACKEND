package com.project.shopapp.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Table(name="products")
@Entity
@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor

public class Product extends BaseEntity{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 350)
    private String name;

    private Float price;

    @Column(length = 300)
    private String thumbnail;

    private String description;

    @ManyToOne
    @JoinColumn(name="category_id")
    private Category category;


    @OneToMany(mappedBy = "product",
    cascade = CascadeType.ALL,
    fetch = FetchType.LAZY)
    @JsonIgnore
    private List<ProductImage> productImages;

    @JsonIgnore
    @OneToMany(mappedBy = "product",
            cascade = CascadeType.ALL,
            fetch = FetchType.LAZY)
    private List<Rating> ratings = new ArrayList<>();

    @Column(name = "number_of_rating")
    private Long numberOfRating;

    @Column(name = "sum_of_rating")
    private Long sumOfRating;

    @Override
    public String toString() {
        return "Product{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", price=" + price +

                ", numberOfRating=" + numberOfRating +
                ", sumOfRating=" + sumOfRating +
                '}';
    }
}
