package com.nikookinn.librarymanagement.service;

import com.nikookinn.librarymanagement.dto.request.CategoryCreateRequest;
import com.nikookinn.librarymanagement.dto.response.CategoryResponse;
import com.nikookinn.librarymanagement.dto.request.CategoryUpdateRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CategoryService {
    Page<CategoryResponse> getAllCategories(Pageable pageable);
    CategoryResponse getCategoryById(Long id);
    CategoryResponse createCategory(CategoryCreateRequest request);
    CategoryResponse updateCategory(Long id, CategoryUpdateRequest request);
    void deleteCategory(Long id);
}
