package com.project.shopapp.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.project.shopapp.dto.ProductDTO;
import com.project.shopapp.dto.ProductDetailDTO;
import com.project.shopapp.dto.ProductImageDTO;
import com.project.shopapp.exception.DataNotFoundException;
import com.project.shopapp.exception.InvalidParamException;
import com.project.shopapp.models.Product;
import com.project.shopapp.models.ProductImage;
import com.project.shopapp.request.ProductRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface IProductService extends IBaseRedisService {
    public Product createProduct(ProductDTO productDTO) throws DataNotFoundException;

    Page<ProductDTO> getAllProduct(String keyWord,Long categoryId,PageRequest pageRequest);
    List<?> getAllProduct(String keyword, Long categoryId, Pageable pageable, Class<?> clazz) throws JsonProcessingException;

    Product updateProduct(Long id, ProductRequest productRequest) throws Exception;

    Product getProductById(long productId) throws Exception;

    public ProductDetailDTO getProductDetail(Long id) throws DataNotFoundException;
    void deleteProduct(Long id);

    boolean existsByName(String name);
    public ProductImage getProductImageById(Long id);
    public ProductImage createProductImages(Long productId, ProductImageDTO productImageDTO) throws InvalidParamException, DataNotFoundException;

    public List<ProductImage> findAllProductImages(List<Long> ids) throws DataNotFoundException;



    public void deleteProductImage(Long id);


}
