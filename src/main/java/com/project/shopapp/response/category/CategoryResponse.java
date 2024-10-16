package com.project.shopapp.response.category;

import com.project.shopapp.models.Category;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CategoryResponse {
    private String message;
    private Category category;
}
