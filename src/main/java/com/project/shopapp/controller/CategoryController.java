package com.project.shopapp.controller;

import com.project.shopapp.components.LocalizationUtils;
import com.project.shopapp.components.converters.CategoryMessageConverter;
import com.project.shopapp.dto.CategoryDTO;
import com.project.shopapp.models.Category;
import com.project.shopapp.response.ResponseObject;
import com.project.shopapp.response.category.CategoryResponse;
import com.project.shopapp.service.ICategoryService;
import com.project.shopapp.untils.MessageKeys;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("${api.prefix}/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final ICategoryService iCategoryService;
    private final LocalizationUtils localizationUtils;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PostMapping("")
    public ResponseEntity<ResponseObject> createCategory(@Valid @RequestBody CategoryDTO categoryDTO,
                                            BindingResult bindingResult){
        if(bindingResult.hasErrors()){
            List<String> errorMessage = bindingResult.getFieldErrors().stream()
                    .map(fieldError -> fieldError.getDefaultMessage()).toList();
            return ResponseEntity.badRequest().body(ResponseObject.builder()
                            .message(String.join(";", errorMessage))
                            .status(HttpStatus.BAD_REQUEST.value())
                    .build());
        }
        Category category = iCategoryService.createCategory(categoryDTO);
        return ResponseEntity.ok(ResponseObject.builder()
                        .message(localizationUtils.getLocalizeMessage(MessageKeys.INSERT_CATEGORY_SUCCESSFULLY))
                        .status(HttpStatus.OK.value())
                        .data(category)
                .build());
    }

    @GetMapping("")
    public ResponseEntity<ResponseObject> getAllCategories() throws Exception{
        List<Category> categories = iCategoryService.getAllCategory();

        return ResponseEntity.ok(ResponseObject.builder()
                        .status(HttpStatus.OK.value())
                        .message("Lấy danh sách danh mục thành công")
                        .data(categories)
                .build());
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<ResponseObject> updateCategory(@PathVariable Long id,@Valid @RequestBody CategoryDTO categoryDTO) throws Exception{
        return ResponseEntity.ok(ResponseObject.builder()
                .data(iCategoryService.updateCategory(id,categoryDTO))
                .status(HttpStatus.OK.value())
                .message(localizationUtils.getLocalizeMessage(MessageKeys.UPDATE_CATEGORY_SUCCESSFULLY))
                .build());


    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<ResponseObject> deleteCategory(@PathVariable Long id) throws Exception{
            iCategoryService.deleteCategory(id);
            return ResponseEntity.ok(ResponseObject.builder()
                    .message(localizationUtils.getLocalizeMessage(MessageKeys.DELETE_CATEGORY_SUCCESSFULLY,id))
                    .status(HttpStatus.OK.value())
                    .build());
    }
}
