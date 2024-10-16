package com.project.shopapp.controller;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.project.shopapp.components.LocalizationUtils;
import com.project.shopapp.dto.ProductDTO;
import com.project.shopapp.dto.ProductDetailDTO;
import com.project.shopapp.dto.ProductImageDTO;
import com.project.shopapp.exception.DataNotFoundException;
import com.project.shopapp.exception.InvalidParamException;
import com.project.shopapp.mapper.ProductMapper;
import com.project.shopapp.models.Product;
import com.project.shopapp.models.ProductImage;
import com.project.shopapp.request.ProductRequest;
import com.project.shopapp.response.product.ProductDetailsResponse;

import com.project.shopapp.response.product.ProductListResponse;
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
    @PostMapping()
    public ResponseEntity<?> createProduct(@Valid @RequestBody ProductDTO productDTO
            , BindingResult bindingResult){
        try {
            if(bindingResult.hasErrors()) {
                List<String> errorMessage = bindingResult.getFieldErrors()
                        .stream()
                        .map(fieldError -> fieldError.getDefaultMessage()).toList();
                return ResponseEntity.badRequest().body(errorMessage);
            }
            Product newProduct = productService.createProduct(productDTO);
            ProductDTO productDTO1 = ProductMapper.MAPPER.mapToProductDTO(newProduct);
            productDTO1.setCategoryId(newProduct.getCategory().getId());
            return ResponseEntity.ok(ProductResponse.builder()
                            .message(localizationUtils.getLocalizeMessage(MessageKeys.INSERT_PRODUCT_SUCCESSFULLY))
                            .product(productDTO1).build());
        }
        catch(Exception e){
            return ResponseEntity.badRequest().body(e.getMessage());
        }

    }
    @PreAuthorize("true")
    @GetMapping("")
    public ResponseEntity<?> getAllProducts(
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(defaultValue = "0",name = "category_id") Long categoryId,
            @RequestParam(defaultValue = "0",name="page") int page,
            @RequestParam(defaultValue = "10",name="limit") int limit) throws JsonProcessingException {
        //tao pageable tu thong tin trang va gioi han
        int totalPage = 0;
        long totalElements = 0;
        PageRequest pageRequest = PageRequest.of(page,limit, Sort.by("id").ascending());
        logger.info(String.format("keyword = %s, category_id = %d, page = %d, limit = %d", keyword, categoryId, page, limit));
        String key = "ALL_PRODUCTS";
        List<?> products = productService.getAllProduct(keyword,categoryId,pageRequest,ProductDTO.class);
        if(products.size() == 0){
            Page<ProductDTO> productPage = productService.getAllProduct(keyword,categoryId,pageRequest);
            totalPage = productPage.getTotalPages();
            totalElements = productPage.getTotalElements();
            products = productPage.getContent();
            productService.saveList(key,products);
        }

        return ResponseEntity.ok(ProductListResponse.builder()
                .products((List<ProductDTO>) products)
                .totalElements(totalElements)
                .totalPages(totalPage)
                .build());
    };

    @GetMapping("/{id}")
    public ResponseEntity<?> getProductDetail(
            @PathVariable("id") Long productId){
        try {
            ProductDetailDTO productDetailDTO = productService.getProductDetail(productId);
            return ResponseEntity.ok(ProductDetailsResponse.builder()
                            .message(localizationUtils.getLocalizeMessage(MessageKeys.GET_PRODUCT_SUCCESSFULLY))
                            .productDetailDTO(productDetailDTO)
                    .build());
        }catch (Exception e)
        {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteProduct(@PathVariable Long id){
        try{
            productService.deleteProduct(id);
            return ResponseEntity.ok(
                    ProductResponse.builder()
                            .message(localizationUtils.getLocalizeMessage(MessageKeys.DELETE_PRODUCT_SUCCESSFULLY,id))
                            .product(null)
                            .build()
                   );
        }catch (Exception e){
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    private String checkImage(MultipartFile file){
        if(file.getSize() == 0){
            return localizationUtils.getLocalizeMessage(MessageKeys.UPLOAD_IMAGES_FILE_NO_IMAGES);
        }
        //kiem tra dung luong anh va kich thuoc, kich thuoc lon hon 10MB
        if(file.getSize() > 10 * 1024 * 1024) {
            return localizationUtils.getLocalizeMessage(MessageKeys.UPLOAD_IMAGES_FILE_LARGE);

        }
        return null;
    }
    private List<ProductImage> storeToDB(String filename,Product existingProduct,List<ProductImage> productImages) throws InvalidParamException, DataNotFoundException {
        ProductImageDTO productImageDTO = new ProductImageDTO();
        productImageDTO.setImageUrl(filename);
        ProductImage productImageCreate = productService.createProductImages(existingProduct.getId(), productImageDTO);
        productImages.add(productImageCreate);
        return productImages;
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PutMapping(value = "/upload/{id}",consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> updateImages(@PathVariable("id") Long id,
                                          @RequestParam("images") List<MultipartFile> files,
                                          @RequestParam(name="imageIds",required = false) List<Long> imageIds,
                                          @RequestParam("updateImages") List<String> updateImage) throws IOException,Exception {
        try {
            Product existingProduct = productService.getProductById(id);
            files = files == null ? new ArrayList<MultipartFile>() : files;
            if(files.size() > ProductImage.MAXIMUM_IMAGES_PER_PRODUCT){
                return ResponseEntity.badRequest().body(
                        ProductResponse.builder()
                                .message(localizationUtils.getLocalizeMessage(MessageKeys.UPLOAD_IMAGES_FILE_LARGE))
                                .product(null)
                );
            }
            int i = 0;
            List<ProductImage> productImages = new ArrayList<>();
            for (MultipartFile file : files){
                    String checkImage = checkImage(file);
                   if(checkImage != null){
                       return ResponseEntity.badRequest().body(checkImage);
                   }

                String filename = storageService.uploadFile(file);
                if(imageIds != null && updateImage.get(i).equals("false")){
                    ProductImage productImage = productService.getProductImageById(imageIds.get(i));
                    productService.deleteProductImage( imageIds.get(i));
                    storageService.deleteFile(productImage.getImageUrl());
                }
                i++;
                //Luu vao doi tuong ProductImages trong DB
                storeToDB(filename,existingProduct,productImages);

            }
            return ResponseEntity.ok().body(productImages);
        } catch (DataNotFoundException | InvalidParamException e) {
            return  ResponseEntity.badRequest().body(e.getMessage());
        }
    }
//    private void deleteFile(String filename) throws IOException {
//        Path uploadDir = Paths.get("uploads");
//        Path fileToDelete = uploadDir.resolve(filename);
//        if (Files.exists(fileToDelete)) {
//            // Xóa file
//            logger.info("xoa anh thanh` cong : " + filename);
//            Files.delete(fileToDelete);
//
//        } else {
//            throw new IOException("File not found: " + filename);
//
//        }
//    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PostMapping(value = "/upload/{id}",consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> uploadImages(@ModelAttribute("files") List<MultipartFile> files, @PathVariable Long id) throws IOException,Exception {
        try {
            Product existingProduct = productService.getProductById(id);
            files = files == null ? new ArrayList<MultipartFile>() : files;
            if(files.size() > ProductImage.MAXIMUM_IMAGES_PER_PRODUCT){
                return ResponseEntity.badRequest().body(
                        ProductResponse.builder()
                                        .message(localizationUtils.getLocalizeMessage(MessageKeys.UPLOAD_IMAGES_FILE_LARGE))
                                .product(null)
                        );
            }
            List<ProductImage> productImages = new ArrayList<>();
            for (MultipartFile file : files){
                String checkImage = checkImage(file);
                if(checkImage != null){
                    return ResponseEntity.badRequest().body(checkImage);
                }
                String filename = storageService.uploadFile(file);

                //Luu vao doi tuong ProductImages trong DB
               storeToDB(filename,existingProduct,productImages);
            }
            return ResponseEntity.ok().body(productImages);
        } catch (DataNotFoundException | InvalidParamException e) {
            return  ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @DeleteMapping("/images/{fileName}")
    public ResponseEntity<String> deleteFile(@PathVariable String fileName) {
        return new ResponseEntity<>(storageService.deleteFile(fileName), HttpStatus.OK);
    }

    @GetMapping("/images/{fileName}")
    public ResponseEntity<?> viewImage(@PathVariable String fileName){
        try {
            byte[] image = storageService.viewImage(fileName);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.IMAGE_JPEG); // Set the content type based on your image type
            return new ResponseEntity<>(image, headers, HttpStatus.OK);
        } catch (IOException e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PutMapping("/update/{id}")
    public ResponseEntity<?> updateProduct(@PathVariable Long id, @RequestBody ProductRequest productRequest){
        try {
            return ResponseEntity.ok().body(productService.updateProduct(id,productRequest));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

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
