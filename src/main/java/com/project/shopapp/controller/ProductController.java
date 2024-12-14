package com.project.shopapp.controller;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.project.shopapp.components.LocalizationUtils;
import com.project.shopapp.dto.*;
import com.project.shopapp.exceptions.DataNotFoundException;
import com.project.shopapp.exceptions.InvalidParamException;
import com.project.shopapp.mapper.ProductMapper;
import com.project.shopapp.models.Product;
import com.project.shopapp.models.ProductImage;
import com.project.shopapp.request.ProductRequest;
import com.project.shopapp.response.ResponseObject;

import com.project.shopapp.response.product.ProductListResponse;
import com.project.shopapp.response.product.ProductMaxAndMinPriceResponse;
import com.project.shopapp.response.product.ProductResponse;
import com.project.shopapp.service.IProductService;
import com.project.shopapp.service.IStorageService;
import com.project.shopapp.untils.MessageKeys;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.HttpHeaders;
import java.io.IOException;

import java.util.ArrayList;
import java.util.List;


@RestController
@RequestMapping("${api.prefix}/products")
@RequiredArgsConstructor

public class ProductController {

    private final IProductService productService;
    private final LocalizationUtils localizationUtils;
    private final IStorageService storageService;
    private static final Logger logger = LoggerFactory.getLogger(ProductController.class);

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ResponseObject> createProduct(@Valid @RequestPart("product") ProductRequest productRequest, @RequestPart("images") List<MultipartFile> images
            , BindingResult bindingResult) throws Exception {

        if (bindingResult.hasErrors()) {
            List<String> errorMessages = bindingResult.getFieldErrors().stream()
                    .map(fieldError -> fieldError.getField() + ": " + fieldError.getDefaultMessage())
                    .toList();
            return ResponseEntity.badRequest().body(ResponseObject.builder()
                    .message(String.join(", ", errorMessages))
                    .status(HttpStatus.BAD_REQUEST.value())
                    .build());
        }

        Product newProduct = productService.createProduct(productRequest);
            ProductDTO productDTO = ProductMapper.MAPPER.mapToProductDTO(newProduct);
            productDTO.setCategoryId(newProduct.getCategory().getId());
            if(images.size() < 3) {
                return ResponseEntity.badRequest().body(ResponseObject.builder()
                                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                                .message("Cần có ít nhất 3 hình ảnh")
                        .build());
            }
            uploadImages(images, newProduct.getId());
            return ResponseEntity.ok(ResponseObject.builder()
                            .status(HttpStatus.OK.value())
                            .message(localizationUtils.getLocalizeMessage(MessageKeys.INSERT_PRODUCT_SUCCESSFULLY))
                            .data(productDTO)
                    .build());


    }

    @GetMapping("/getPrice")
    public ResponseEntity<ResponseObject> getPriceMaxAndMin(){
        ProductMaxAndMinPriceResponse productMaxAndMinPriceResponse = productService.getMaxAndMinPrice();
        return ResponseEntity.ok(ResponseObject.builder()
                        .data(productMaxAndMinPriceResponse)
                        .message("Lấy thành công.")
                        .status(HttpStatus.OK.value())
                .build());
    }
    @GetMapping("")
    public ResponseEntity<ResponseObject> getAllProducts(
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(defaultValue = "0",name = "category_id") Long categoryId,
            @RequestParam(defaultValue = "10000",name = "minPrice") Float minPrice,
            @RequestParam(defaultValue = "100000",name = "maxPrice") Float maxPrice,
            @RequestParam(defaultValue = "0",name = "rateStar") int rateStar,
            @RequestParam(defaultValue = "0",name="page") int page,
            @RequestParam(defaultValue = "10",name="limit") int limit) throws JsonProcessingException {

        int totalPage = 0;
        long totalElements = 0;
        PageRequest pageRequest = PageRequest.of(page,limit, Sort.by("id").ascending());

        String key = "ALL_PRODUCTS";
        List<?> products = productService.getAllProduct(keyword,categoryId,minPrice,maxPrice,rateStar,pageRequest,ProductDTO.class);
        if(products.size() == 0){
            Page<ProductDTO> productPage = productService.getAllProduct(keyword,categoryId,minPrice,maxPrice,rateStar,pageRequest);
            totalPage = productPage.getTotalPages();
            totalElements = productPage.getTotalElements();
            products = productPage.getContent();
            productService.saveList(key,products);
        }
        ProductListResponse productListResponse = ProductListResponse.builder()
                .products((List<ProductDTO>) products)
                .totalElements(totalElements)
                .totalPages(totalPage)
                .build();
        return ResponseEntity.ok(ResponseObject.builder()
                .data(productListResponse)
                .message(localizationUtils.getLocalizeMessage(MessageKeys.GET_PRODUCT_SUCCESSFULLY))
                .status(HttpStatus.OK.value())
                .build());
    };

    @GetMapping("/{id}")
    public ResponseEntity<ResponseObject> getProductDetail(@PathVariable("id") Long productId) throws DataNotFoundException {

            ProductDetailDTO productDetailDTO = productService.getProductDetail(productId);
            return ResponseEntity.ok(ResponseObject.builder()
                    .message(localizationUtils.getLocalizeMessage(MessageKeys.GET_PRODUCT_SUCCESSFULLY))
                    .data(productDetailDTO)
                    .status(HttpStatus.OK.value())
                    .build());

    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<ResponseObject> deleteProduct(@PathVariable Long id){

            productService.deleteProduct(id);
            return ResponseEntity.ok(ResponseObject.builder()
                    .message(localizationUtils.getLocalizeMessage(MessageKeys.DELETE_PRODUCT_SUCCESSFULLY,id))
                    .status(HttpStatus.OK.value())
                    .build()
                   );

    }
    private String checkImage(MultipartFile file) {
        // Kiểm tra nếu file không có dữ liệu
        if (file.getSize() == 0) {
            return localizationUtils.getLocalizeMessage(MessageKeys.UPLOAD_IMAGES_FILE_NO_IMAGES);
        }

        // Kiểm tra dung lượng file, lớn hơn 10MB
        if (file.getSize() > 10 * 1024 * 1024) {
            return localizationUtils.getLocalizeMessage(MessageKeys.UPLOAD_IMAGES_FILE_LARGE);
        }

        // Kiểm tra file có phải là hình ảnh hay không (kiểm tra loại MIME)
        String mimeType = null;
        try {
            mimeType = file.getContentType();
        } catch (Exception e) {
            return localizationUtils.getLocalizeMessage(MessageKeys.UPLOAD_IMAGES_FILE_MUST_BE_IMAGE);
        }

        // Kiểm tra nếu MIME type không phải là hình ảnh
        if (mimeType == null || !mimeType.startsWith("image/")) {
            return localizationUtils.getLocalizeMessage(MessageKeys.UPLOAD_IMAGES_FILE_MUST_BE_IMAGE);
        }

        // Nếu không có lỗi, trả về null
        return null;
    }

    private List<ProductImage> storeToDB(String filename,Product existingProduct,List<ProductImage> productImages) throws Exception {
        ProductImageDTO productImageDTO = new ProductImageDTO();
        productImageDTO.setImageUrl(filename);
        ProductImage productImageCreate = productService.createProductImages(existingProduct.getId(), productImageDTO);
        productImages.add(productImageCreate);
        return productImages;
    }


    private Product checkMaximumImages(Long productId, List<MultipartFile> files) throws Exception {
        Product existingProduct = productService.getProductById(productId);

        // Kiểm tra nếu files là null thì khởi tạo danh sách rỗng
        files = files == null ? new ArrayList<MultipartFile>() : files;

        // Kiểm tra số lượng hình ảnh
        if (files.size() > ProductImage.MAXIMUM_IMAGES_PER_PRODUCT) {
            throw new Exception(localizationUtils.getLocalizeMessage(MessageKeys.UPLOAD_IMAGES_FILE_LARGE));
        }

        // Trả về existingProduct nếu số lượng hình ảnh hợp lệ
        return existingProduct;
    }


    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PostMapping(value = "/images/upload/{id}",consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ResponseObject> uploadImages(@ModelAttribute("files") List<MultipartFile> files, @PathVariable Long id) throws Exception {

            Product existingProduct = checkMaximumImages(id,files);
            List<ProductImage> productImages = new ArrayList<>();
            for (MultipartFile file : files){
                String checkImage = checkImage(file);
                if(checkImage != null){
                    return ResponseEntity.badRequest().body(ResponseObject.builder()
                                    .status(HttpStatus.BAD_REQUEST.value())
                                    .data(checkImage)
                                    .message(localizationUtils.getLocalizeMessage(MessageKeys.UPLOAD_IMAGES_FILE_MUST_BE_IMAGE))
                            .build());
                }
                String filename = storageService.uploadFile(file);

                //Luu vao doi tuong ProductImages trong DB
               storeToDB(filename,existingProduct,productImages);
            }
            return ResponseEntity.ok().body(ResponseObject.builder()
                            .message("Up load ảnh thành công")
                            .status(HttpStatus.OK.value())
                    .build());

    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @DeleteMapping("/images/{fileName}")
    public ResponseEntity<ResponseObject> deleteFile(@PathVariable String fileName) {
        return ResponseEntity.ok(ResponseObject.builder()
                .status(HttpStatus.OK.value())
                .data(storageService.deleteFile(fileName))
                .message(localizationUtils.getLocalizeMessage(MessageKeys.DELETE_IMAGES_SUCCESSFULLY))
                .build());
    }

    @GetMapping("/images/{fileName}")
    public ResponseEntity<?> viewImage(@PathVariable String fileName) throws IOException {

            byte[] image = storageService.viewImage(fileName);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.IMAGE_JPEG); // Set the content type based on your image type
            return new ResponseEntity<>(image, headers, HttpStatus.OK);

    }
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PutMapping(value="/update/{productId}",consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ResponseObject> updateProduct(@PathVariable Long productId,
                                                        @RequestPart(name="images",required = false) List<MultipartFile> files,
                                                        @RequestPart(name="imageData",required = false) ImageDataDTO imageDataDTO,
                                                        @RequestPart("product") ProductRequest productRequest) throws Exception {
            Product product = productService.updateProduct(productId,productRequest);
            updateImages(productId,files,imageDataDTO);
            return ResponseEntity.ok().body(ResponseObject.builder()
                    .data(product)
                    .message("Cập nhật sản phẩm thành công")
                    .status(HttpStatus.OK.value())
                    .build());

    }

    private void updateImages( Long id,
                               List<MultipartFile> files,
                               ImageDataDTO imageDataDTO) throws Exception {

        Product existingProduct = checkMaximumImages(id,files);
        int i = 0;
        if(files != null) {
            List<ProductImage> productImages = new ArrayList<>();
            for (MultipartFile file : files){
                String checkImage = checkImage(file);
                if(checkImage != null){
                    throw new Exception(checkImage);
                }

                String filename = storageService.uploadFile(file);
                if(imageDataDTO.getImageIds() != null && imageDataDTO.getUpdateImages().get(i).equals("false")){
                    ProductImage productImage = productService.getProductImageById(imageDataDTO.getImageIds().get(i));
                    productService.deleteProductImage(imageDataDTO.getImageIds().get(i));
                    storageService.deleteFile(productImage.getImageUrl());
                }
                i++;
                //Luu vao doi tuong ProductImages trong DB
                storeToDB(filename,existingProduct,productImages);

            }
        }



    }
//    @PreAuthorize("hasRole('ROLE_ADMIN')")
//    @PutMapping(value = "/images/upload/{id}",consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
//    public ResponseEntity<?> updateImages(@PathVariable("id") Long id,
//                                          @RequestParam("images") List<MultipartFile> files,
//                                          @RequestParam(name="imageIds",required = false) List<Long> imageIds,
//                                          @RequestParam("updateImages") List<String> updateImage) throws Exception {
//
//        Product existingProduct = checkMaximumImages(id,files);
//        int i = 0;
//        List<ProductImage> productImages = new ArrayList<>();
//        for (MultipartFile file : files){
//            String checkImage = checkImage(file);
//            if(checkImage != null){
//                return ResponseEntity.badRequest().body(checkImage);
//            }
//
//            String filename = storageService.uploadFile(file);
//            if(imageIds != null && updateImage.get(i).equals("false")){
//                ProductImage productImage = productService.getProductImageById(imageIds.get(i));
//                productService.deleteProductImage(imageIds.get(i));
//                storageService.deleteFile(productImage.getImageUrl());
//            }
//            i++;
//            //Luu vao doi tuong ProductImages trong DB
//            storeToDB(filename,existingProduct,productImages);
//
//        }
//        return ResponseEntity.ok().body(productImages);
//
//    }
//    @GetMapping("/images/{imageName}")
//    public ResponseEntity<?> viewImage(@PathVariable String imageName){
//        try{
//            Path imagePath = Paths.get("uploads/"+imageName);
//            UrlResource resource = new UrlResource(imagePath.toUri());
//            if(resource.exists()){
//                return ResponseEntity.ok()
//                        .contentType(MediaType.IMAGE_JPEG)
//                        .body(resource);
//            }else{
//                return ResponseEntity.notFound().build();
//            }
//        } catch (MalformedURLException e) {
//            return ResponseEntity.notFound().build();
//        }
//    }
//    @PostMapping("/generateFakeProducts")
//    public ResponseEntity<String> generateFakeProducts(){
//        Faker faker = new Faker();
//        for(int i=0;i < 1000;i++){
//            String productName = faker.commerce().productName();
//            if(productService.existsByName(productName)){
//                continue;
//            }
//            ProductDTO productDTO = ProductDTO.builder()
//                    .name(productName)
//                    .price((float)faker.number().numberBetween(10,90000000))
//                    .thumbnail("")
//                    .description(faker.lorem().sentence())
//                    .categoryId((long)faker.number().numberBetween(1,3))
//                    .build();
//            try{
//                productService.createProduct(productDTO);
//            }catch (Exception e){
//                return ResponseEntity.badRequest().body(e.getMessage());
//            }
//        }
//        return ResponseEntity.ok("Fake products create successfully");
//    }
}
