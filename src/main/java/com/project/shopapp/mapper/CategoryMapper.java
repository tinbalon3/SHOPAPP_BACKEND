package com.project.shopapp.mapper;

import com.project.shopapp.dto.CategoryDTO;
import com.project.shopapp.models.Category;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface CategoryMapper {
    CategoryMapper MAPPER = Mappers.getMapper(CategoryMapper.class);
    CategoryDTO mapToCategoryDTO(Category category);

    Category mapToCategory(CategoryDTO categoryDTO);
}
