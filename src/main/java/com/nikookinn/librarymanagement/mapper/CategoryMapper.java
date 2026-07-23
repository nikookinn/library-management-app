package com.nikookinn.librarymanagement.mapper;

import com.nikookinn.librarymanagement.dto.response.CategoryResponse;
import com.nikookinn.librarymanagement.entity.Category;

public final class CategoryMapper {
    private CategoryMapper() {
    }

    public static CategoryResponse toResponse(Category category) {
        return new CategoryResponse(category.getId(), category.getName(), category.getDescription());
    }
}
