package com.nikookinn.librarymanagement.service.impl;

import com.nikookinn.librarymanagement.dto.request.CategoryCreateRequest;
import com.nikookinn.librarymanagement.dto.request.CategoryUpdateRequest;
import com.nikookinn.librarymanagement.entity.Category;
import com.nikookinn.librarymanagement.repository.CategoryRepository;
import com.nikookinn.librarymanagement.service.CategoryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;

@SpringBootTest
@DisplayName("Category Service Integration Tests")
class CategoryServiceIntegrationTest {

    @Autowired
    private CategoryService categoryService;

    @MockitoSpyBean
    private CategoryRepository categoryRepository;

    @BeforeEach
    void setUp() {
        categoryRepository.deleteAll();
    }

    @Test
    @DisplayName("should rollback category creation when save fails")
    void shouldRollbackCategoryCreationWhenSaveFails() {
        CategoryCreateRequest request = new CategoryCreateRequest("Fiction", "Desc");
        
        doThrow(new RuntimeException("Simulated error")).when(categoryRepository).save(any(Category.class));

        assertThatThrownBy(() -> categoryService.createCategory(request))
                .isInstanceOf(RuntimeException.class);

        assertThat(categoryRepository.count()).isZero();
    }

    @Test
    @DisplayName("should rollback category update when save fails")
    void shouldRollbackCategoryUpdateWhenSaveFails() {
        Category category = new Category();
        category.setName("Original");
        final Category savedCategory = categoryRepository.save(category);

        CategoryUpdateRequest request = new CategoryUpdateRequest("Updated", "New Desc");

        doThrow(new RuntimeException("Simulated error")).when(categoryRepository).save(any(Category.class));

        assertThatThrownBy(() -> categoryService.updateCategory(savedCategory.getId(), request))
                .isInstanceOf(RuntimeException.class);

        Category notUpdated = categoryRepository.findById(savedCategory.getId()).orElseThrow();
        assertThat(notUpdated.getName()).isEqualTo("Original");
    }

    @Test
    @DisplayName("should rollback category deletion when delete fails")
    void shouldRollbackCategoryDeletionWhenDeleteFails() {
        Category category = new Category();
        category.setName("To Be Deleted");
        final Category categoryToDelete = categoryRepository.save(category);

        doThrow(new RuntimeException("Simulated error during delete")).when(categoryRepository).deleteById(any(Long.class));

        assertThatThrownBy(() -> categoryService.deleteCategory(categoryToDelete.getId()))
                .isInstanceOf(RuntimeException.class);

        assertThat(categoryRepository.existsById(categoryToDelete.getId())).isTrue();
    }
}
