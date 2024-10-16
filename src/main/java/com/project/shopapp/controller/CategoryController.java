package com.project.shopapp.controller;

import com.project.shopapp.components.LocalizationUtils;
import com.project.shopapp.dto.CategoryDTO;
import com.project.shopapp.models.Category;
import com.project.shopapp.response.category.CategoryResponse;
import com.project.shopapp.service.ICategoryService;
import com.project.shopapp.untils.MessageKeys;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
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

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PostMapping("")
    public ResponseEntity<?> createCategory(@Valid @RequestBody CategoryDTO categoryDTO,
                                            BindingResult bindingResult){
        if(bindingResult.hasErrors()){
            List<String> errorMessage = bindingResult.getFieldErrors().stream()
                    .map(fieldError -> fieldError.getDefaultMessage()).toList();
            return ResponseEntity.badRequest().body(errorMessage);
        }
        return ResponseEntity.ok(CategoryResponse.builder()
                        .message(localizationUtils.getLocalizeMessage(MessageKeys.INSERT_CATEGORY_SUCCESSFULLY))
                        .category(iCategoryService.createCategory(categoryDTO))
                .build());
    }

    @GetMapping("")
    public ResponseEntity<List<Category>> getAllCategories(){
        List<Category> categories = iCategoryService.getAllCategory();
        return ResponseEntity.ok(categories);
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<CategoryResponse> updateCategory(@PathVariable Long id,@Valid @RequestBody CategoryDTO categoryDTO){
        try{
            return ResponseEntity.ok(CategoryResponse.builder()
                    .message(localizationUtils.getLocalizeMessage(MessageKeys.UPDATE_CATEGORY_SUCCESSFULLY))
                    .category(iCategoryService.updateCategory(id,categoryDTO))
                    .build());
        }catch (Exception e){
            return ResponseEntity.badRequest().body(CategoryResponse.builder()
                    .message(e.getMessage())
                    .category(null)
                    .build());
        }

    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<CategoryResponse> deleteCategory(@PathVariable Long id){
        try {
            iCategoryService.deleteCategory(id);
            return ResponseEntity.ok(CategoryResponse.builder()
                    .message(localizationUtils.getLocalizeMessage(MessageKeys.DELETE_CATEGORY_SUCCESSFULLY,id))
                    .category(null)
                    .build());
        }catch (Exception e){
            return ResponseEntity.ok(CategoryResponse.builder()
                    .message(e.getMessage())
                    .category(null)
                    .build());
        }

    }
}
