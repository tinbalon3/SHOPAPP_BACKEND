package com.project.shopapp.mapper;


import com.project.shopapp.dto.ProductDTO;
import com.project.shopapp.dto.ProductDetailDTO;

import com.project.shopapp.models.Product;
import com.project.shopapp.request.ProductRequest;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;


@Mapper
public interface ProductMapper {
    ProductMapper MAPPER = Mappers.getMapper(ProductMapper.class);
    ProductDTO mapToProductDTO(Product product);

    Product mapToProduct(ProductDTO productDTO);

    ProductDetailDTO mapToProductDetailDTO(Product product);

    Product mapToProduct(ProductRequest productRequest);
}
