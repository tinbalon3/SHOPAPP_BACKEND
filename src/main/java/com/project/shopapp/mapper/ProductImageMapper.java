package com.project.shopapp.mapper;

import com.project.shopapp.dto.OrderDetailDTO;
import com.project.shopapp.dto.ProductDTO;
import com.project.shopapp.dto.ProductDetailDTO;
import com.project.shopapp.dto.ProductImageDTO;
import com.project.shopapp.models.OrderDetail;
import com.project.shopapp.models.Product;
import com.project.shopapp.models.ProductImage;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper
public interface ProductImageMapper {
    ProductImageMapper MAPPER = Mappers.getMapper(ProductImageMapper.class);
    ProductImageDTO mapToProductImageDTO(ProductImage product);

    ProductImage mapToProductImage(ProductImageDTO productDTO);
    List<ProductImageDTO> mapToProductImageDTOList(List<ProductImage> productImages);
}
