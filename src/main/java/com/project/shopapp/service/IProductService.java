package com.project.shopapp.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.project.shopapp.dto.ProductDTO;
import com.project.shopapp.dto.ProductDetailDTO;
import com.project.shopapp.dto.ProductImageDTO;
import com.project.shopapp.exceptions.DataNotFoundException;
import com.project.shopapp.exceptions.InvalidParamException;
import com.project.shopapp.models.Product;
import com.project.shopapp.models.ProductImage;
import com.project.shopapp.request.ProductRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface IProductService extends IBaseRedisService {
    public Product createProduct(ProductDTO productDTO) throws DataNotFoundException;

    Page<ProductDTO> getAllProduct(String keyWord,Long categoryId,Float minPrice,Float maxPrice,int rateStar,PageRequest pageRequest);
    List<?> getAllProduct(String keyword, Long categoryId,Float minPrice, Float maxPrice,int rateStar, Pageable pageable, Class<?> clazz) throws JsonProcessingException;

    Product updateProduct(Long id, ProductRequest productRequest) throws Exception;

    Product getProductById(long productId) throws DataNotFoundException;

    public ProductDetailDTO getProductDetail(Long id) throws DataNotFoundException;
    void deleteProduct(Long id);

    public ProductImage getProductImageById(Long id);
    public ProductImage createProductImages(Long productId, ProductImageDTO productImageDTO) throws InvalidParamException, DataNotFoundException;


    public void deleteProductImage(Long id);

    public List<Product> getAllProduct();


}
