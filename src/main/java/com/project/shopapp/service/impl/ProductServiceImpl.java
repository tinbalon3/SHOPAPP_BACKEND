package com.project.shopapp.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.shopapp.components.LocalizationUtils;
import com.project.shopapp.dto.ProductDTO;
import com.project.shopapp.dto.ProductDetailDTO;
import com.project.shopapp.dto.ProductImageDTO;
import com.project.shopapp.exception.DataNotFoundException;
import com.project.shopapp.exception.InvalidParamException;
import com.project.shopapp.mapper.ProductImageMapper;
import com.project.shopapp.mapper.ProductMapper;
import com.project.shopapp.models.Category;
import com.project.shopapp.models.Product;
import com.project.shopapp.models.ProductImage;
import com.project.shopapp.repositories.CategoryRepository;
import com.project.shopapp.repositories.ProductImageRepository;
import com.project.shopapp.repositories.ProductRepository;
import com.project.shopapp.request.ProductRequest;
import com.project.shopapp.service.IProductService;
import com.project.shopapp.untils.MessageKeys;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service

//@AllArgsConstructor

public class ProductServiceImpl extends BaseRedisServiceImpl implements IProductService {


    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ProductImageRepository productImageRepository;
    private final LocalizationUtils localizationUtils;
    @Autowired
    public ProductServiceImpl(RedisTemplate<String, Object> redisTemplate, ProductRepository productRepository, CategoryRepository categoryRepository, ProductImageRepository productImageRepository, LocalizationUtils localizationUtils) {
        super(redisTemplate);
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.productImageRepository = productImageRepository;
        this.localizationUtils = localizationUtils;
    }

    @Override
    @Transactional
    public Product createProduct(ProductDTO productDTO) throws DataNotFoundException {

        Category existingCategory =
                categoryRepository
                        .findById(productDTO.getCategoryId()).orElseThrow(() -> new DataNotFoundException(
                        localizationUtils.getLocalizeMessage(MessageKeys.NOT_FOUND_CATEGORY,productDTO.getCategoryId())));

        Product newProduct = ProductMapper.MAPPER.mapToProduct(productDTO);
        newProduct.setCategory(existingCategory);
        return productRepository.save(newProduct);
    }
    @Override
    public Product getProductById(long productId) throws Exception {
        return productRepository.findById(productId).
                orElseThrow(()-> new DataNotFoundException(
                        localizationUtils.getLocalizeMessage(MessageKeys.NOT_FOUND_PRODUCT,productId)));
    }
    @Override
    public ProductDetailDTO getProductDetail(Long id) throws DataNotFoundException {
        Product product = productRepository.findById(id).orElseThrow(
                () -> new DataNotFoundException(
                        localizationUtils.getLocalizeMessage(MessageKeys.NOT_FOUND_PRODUCT,id)));
        List<ProductImageDTO> productImages = productImageRepository.findByProductId(id)
                .stream()
                .map(productImage -> {
                    ProductImageDTO imageDTO = ProductImageMapper.MAPPER.mapToProductImageDTO(productImage);
                    imageDTO.setProductId(product.getId());
                    imageDTO.setId(productImage.getId());
                    return imageDTO;
                })
                .collect(Collectors.toList());

        ProductDetailDTO productDetailDTO = ProductMapper.MAPPER.mapToProductDetailDTO(product);
        productDetailDTO.setCategoryId(product.getCategory().getId());
        productDetailDTO.setProduct_images(productImages);
        return productDetailDTO;
    }
    @Override
    public List<ProductImage> findAllProductImages(List<Long> ids) throws DataNotFoundException {
        List<ProductImage> productImage = productImageRepository.findAllById(ids);
        return productImage;
    }
    @Override
    @Transactional
    public void deleteProductImage(Long id){
        productImageRepository.deleteById(id);
    }
    @Override
    public Page<ProductDTO> getAllProduct(
            String keyWord,
            Long categoryId,
            PageRequest pageRequest) {
        //lay danh sach san pham theo page va limit
        return productRepository.searchProducts(categoryId,keyWord,pageRequest).map(product -> {
            ProductDTO productEntity = ProductMapper.MAPPER.mapToProductDTO(product);
            productEntity.setCategoryId(product.getCategory().getId());
            productEntity.setId(product.getId());
            return productEntity;
        });
    }
    private String getKeyFrom(String keyword, Long categoryId, Pageable pageable) {
        int pageNumber = pageable.getPageNumber();
        int pageSize = pageable.getPageSize();
        Sort sort = pageable.getSort();

        // Xác định hướng sắp xếp (asc/desc) cho trường "id"
        String sortDirection = sort.getOrderFor("id").getDirection() == Sort.Direction.ASC ? "asc" : "desc";

        // Chuẩn hóa keyword để loại bỏ các ký tự không cần thiết và tránh xung đột key
        String normalizedKeyword = keyword != null ? keyword.replaceAll("\\s+", "_").toLowerCase() : "all";

        // Thêm categoryId vào key nếu tồn tại, nếu không thì mặc định là "all"
        String categoryPart = categoryId != null ? categoryId.toString() : "all";

        // Tạo chuỗi key với keyword, categoryId, pageNumber, pageSize và sortDirection
        String key = String.format("ALL_PRODUCTS:%s:%s:%d:%d:%s",
                normalizedKeyword,
                categoryPart,
                pageNumber,
                pageSize,
                sortDirection);

        return key;
    }

    @Override
    public List<?> getAllProduct(String keyword, Long categoryId, Pageable pageable, Class<?> clazz) throws JsonProcessingException {
        String key = this.getKeyFrom(keyword,categoryId,pageable);
        String json = (String) redisTemplate.opsForValue().get(key);
        if (json != null) {
            ObjectMapper redisObjectMapper = new ObjectMapper();
            return redisObjectMapper.readValue(json, redisObjectMapper.getTypeFactory().constructCollectionType(List.class, clazz));
        }
        return  Collections.emptyList();
    }

    @Override
    @Transactional
    public Product updateProduct(Long id, ProductRequest productRequest) throws Exception {

        Category existingCategory =
                categoryRepository.
                        findById(productRequest.getCategoryId()).orElseThrow(() ->
                                new DataNotFoundException(
                                        localizationUtils.getLocalizeMessage
                                                (MessageKeys.NOT_FOUND_CATEGORY,productRequest.getCategoryId())));
        Product existingProduct = getProductById(id);
        if (existingProduct != null) {
            // Update only the fields from the request
            if (productRequest.getName() != null) {
                existingProduct.setName(productRequest.getName());
            }
            if (productRequest.getPrice() >= 0) {
                existingProduct.setPrice(productRequest.getPrice());
            }
            if (productRequest.getDescription() != null) {
                existingProduct.setDescription(productRequest.getDescription());
            }
            if (productRequest.getCategoryId() > 0) {

                existingProduct.setCategory(existingCategory);
            }
            // Save the updated product

            return productRepository.save(existingProduct);
        }
        return null;
    }

    @Override
    @Transactional
    public void deleteProduct(Long id) {
        Optional<Product> optionalProduct = productRepository.findById(id);
        optionalProduct.ifPresent(productRepository::delete);
    }

    @Override
    public boolean existsByName(String name) {
        return productRepository.existsByName(name);
    }
    @Override
    @Transactional
    public ProductImage createProductImages(Long productId, ProductImageDTO productImageDTO) throws DataNotFoundException, InvalidParamException {
        Product existingProduct =
                productRepository
                        .findById(productId).orElseThrow(
                                () -> new DataNotFoundException(
                                        localizationUtils.getLocalizeMessage(
                                                MessageKeys.NOT_FOUND_PRODUCT,productId)));
        ProductImage newProductImage = ProductImageMapper.MAPPER.mapToProductImage(productImageDTO);
        newProductImage.setProduct(existingProduct);

        //khong cho insert qua 5 anh cho 1 san pham
        int size = productImageRepository.findByProductId(productId).size();
        if(size >= ProductImage.MAXIMUM_IMAGES_PER_PRODUCT){
            throw new InvalidParamException(localizationUtils.getLocalizeMessage(MessageKeys.UPLOAD_IMAGES_MAX_5));
        }
        return productImageRepository.save(newProductImage);
    }
    @Override
    public ProductImage getProductImageById(Long id){
        return productImageRepository.findById(id).orElseThrow(() ->
                new RuntimeException("Khong tim thay anh"));
    }
}
