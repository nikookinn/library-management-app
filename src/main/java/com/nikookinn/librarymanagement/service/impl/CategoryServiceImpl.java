package com.nikookinn.librarymanagement.service.impl;

import com.nikookinn.librarymanagement.dto.request.CategoryCreateRequest;
import com.nikookinn.librarymanagement.dto.response.CategoryResponse;
import com.nikookinn.librarymanagement.dto.request.CategoryUpdateRequest;
import com.nikookinn.librarymanagement.entity.Category;
import com.nikookinn.librarymanagement.exception.ResourceNotFoundException;
import com.nikookinn.librarymanagement.mapper.CategoryMapper;
import com.nikookinn.librarymanagement.repository.CategoryRepository;
import com.nikookinn.librarymanagement.service.CategoryService;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class CategoryServiceImpl implements CategoryService {
    private final CategoryRepository categoryRepository;

    public CategoryServiceImpl(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @Override
    @Cacheable(value = "categories", key = "#pageable")
    public Page<CategoryResponse> getAllCategories(Pageable pageable) {
        return categoryRepository.findAll(pageable)
                .map(CategoryMapper::toResponse);
    }

    @Override
    @Cacheable(value = "categories", key = "#id")
    public CategoryResponse getCategoryById(Long id) {
        return categoryRepository.findById(id)
                .map(CategoryMapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + id));
    }

    @Override
    @Transactional
    @CacheEvict(value = "categories", allEntries = true)
    public CategoryResponse createCategory(CategoryCreateRequest request) {
        Category category = new Category();
        category.setName(request.name());
        category.setDescription(request.description());

        Category saved = categoryRepository.save(category);
        return CategoryMapper.toResponse(saved);
    }

    @Override
    @Transactional
    @CacheEvict(value = {"categories", "books"}, allEntries = true)
    public CategoryResponse updateCategory(Long id, CategoryUpdateRequest request) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + id));

        category.setName(request.name());
        category.setDescription(request.description());

        Category updated = categoryRepository.save(category);
        return CategoryMapper.toResponse(updated);
    }

    @Override
    @Transactional
    @CacheEvict(value = {"categories", "books"}, allEntries = true)
    public void deleteCategory(Long id) {
        if (!categoryRepository.existsById(id)) {
            throw new ResourceNotFoundException("Category not found with id: " + id);
        }
        categoryRepository.deleteById(id);
    }

}
