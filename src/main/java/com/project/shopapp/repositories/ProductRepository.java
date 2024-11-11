package com.project.shopapp.repositories;

import com.project.shopapp.models.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;


public interface ProductRepository extends JpaRepository<Product,Long> {
    boolean existsByName(String name);

    Page<Product> findAll(Pageable pageable); //phan trang


    @Query("SELECT p FROM Product p WHERE " +
            "(:categoryId IS NULL OR :categoryId = 0 OR p.category.id = :categoryId) AND " +
            "(:minPrice IS NULL OR :maxPrice IS NULL OR p.price >= :minPrice AND p.price <= :maxPrice) AND " +
            "(:rateStar = 0 OR :rateStar = FLOOR(p.sumOfRating / p.numberOfRating)) AND " +  // Làm tròn xuống phần sàn
            "(:keyword IS NULL OR :keyword = '' OR p.name LIKE %:keyword% OR p.description LIKE %:keyword%)")
    Page<Product> searchProducts(@Param("categoryId") Long categoryId,
                                 @Param("keyword") String keyword,
                                 @Param("minPrice") float minPrice,
                                 @Param("maxPrice") float maxPrice,
                                 @Param("rateStar") float rateStar,
                                 Pageable pageable);
}
