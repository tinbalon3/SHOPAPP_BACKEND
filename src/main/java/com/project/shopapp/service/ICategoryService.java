package com.project.shopapp.service;

import com.project.shopapp.dto.CategoryDTO;
import com.project.shopapp.models.Category;

import java.util.List;

public interface ICategoryService {
    Category createCategory(CategoryDTO category);

    Category getCategoryById(Long id);
    List<Category> getAllCategory();

    Category updateCategory(Long categoryId, CategoryDTO category);
    void deleteCategory(Long id) throws Exception;
}
