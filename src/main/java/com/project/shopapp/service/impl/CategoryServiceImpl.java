package com.project.shopapp.service.impl;

import com.project.shopapp.components.LocalizationUtils;
import com.project.shopapp.dto.CategoryDTO;
import com.project.shopapp.mapper.CategoryMapper;
import com.project.shopapp.models.Category;
import com.project.shopapp.repositories.CategoryRepository;
import com.project.shopapp.service.ICategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements ICategoryService {

    private final CategoryRepository categoryRepository;
    private final LocalizationUtils localizationUtils;


    @Override
    public Category createCategory(CategoryDTO categoryDTO) {

        Category category = CategoryMapper.MAPPER.mapToCategory(categoryDTO);
        category.setIsActive(true);
        return categoryRepository.save(category);
    }

    @Override
    public Category getCategoryById(Long id) {
        return categoryRepository.findByIdAndIsActiveTrue(id);
    }

    @Override
    public List<Category> getAllCategory() {
        return categoryRepository.findAllByIsActiveTrue();
    }

    @Override
    @Transactional
    public Category updateCategory(Long categoryId, CategoryDTO categoryDTO) {
        Category existingCategory = getCategoryById(categoryId);
        existingCategory.setName(categoryDTO.getName());
        categoryRepository.save(existingCategory);
        return existingCategory;
    }

    @Override
    @Transactional
    public void deleteCategory(Long id) throws Exception {
        try{
            Optional<Category> category = categoryRepository.findById(id);
            if(category.isPresent()){
                category.get().setIsActive(false);
            }
            categoryRepository.save(category.get());

        }catch (Exception e){
            throw new Exception(e.getMessage());
        }

    }
}
