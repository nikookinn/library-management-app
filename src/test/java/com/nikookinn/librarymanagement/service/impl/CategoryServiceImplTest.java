package com.nikookinn.librarymanagement.service.impl;

import com.nikookinn.librarymanagement.dto.request.CategoryCreateRequest;
import com.nikookinn.librarymanagement.dto.request.CategoryUpdateRequest;
import com.nikookinn.librarymanagement.dto.response.CategoryResponse;
import com.nikookinn.librarymanagement.entity.Category;
import com.nikookinn.librarymanagement.exception.ResourceNotFoundException;
import com.nikookinn.librarymanagement.repository.CategoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Category Service Unit Tests")
class CategoryServiceImplTest {

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private CategoryServiceImpl categoryService;

    private Category category;

    @BeforeEach
    void setUp() {
        category = new Category();
        category.setId(1L);
        category.setName("Fantasy");
        category.setDescription("Fantasy Literature");
    }

    @Nested
    @DisplayName("getAllCategories")
    class GetAllCategories {
        @Test
        @DisplayName("should return all categories with pagination")
        void shouldGetAllCategories() {
            // Arrange
            Pageable pageable = PageRequest.of(0, 10);
            Page<Category> categoryPage = new PageImpl<>(List.of(category));
            when(categoryRepository.findAll(pageable)).thenReturn(categoryPage);

            // Act
            Page<CategoryResponse> result = categoryService.getAllCategories(pageable);

            // Assert
            assertThat(result.getContent()).hasSize(1);
            verify(categoryRepository).findAll(pageable);
        }
    }

    @Nested
    @DisplayName("createCategory")
    class CreateCategory {
        @Test
        @DisplayName("should create and return category")
        void shouldCreateCategory() {
            // Arrange
            CategoryCreateRequest request = new CategoryCreateRequest("Fantasy", "Fantasy Literature");
            when(categoryRepository.save(any(Category.class))).thenReturn(category);

            // Act
            CategoryResponse result = categoryService.createCategory(request);

            // Assert
            assertThat(result.name()).isEqualTo("Fantasy");
            verify(categoryRepository).save(any(Category.class));
        }
    }

    @Nested
    @DisplayName("getCategoryById")
    class GetCategoryById {
        @Test
        @DisplayName("should return category when valid ID is provided")
        void shouldGetCategoryById() {
            // Arrange
            when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));

            // Act
            CategoryResponse result = categoryService.getCategoryById(1L);

            // Assert
            assertThat(result.name()).isEqualTo("Fantasy");
            verify(categoryRepository).findById(1L);
        }

        @Test
        @DisplayName("should throw ResourceNotFoundException when category not found")
        void shouldThrowExceptionWhenCategoryNotFound() {
            // Arrange
            when(categoryRepository.findById(1L)).thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> categoryService.getCategoryById(1L))
                    .isInstanceOf(ResourceNotFoundException.class);
            verify(categoryRepository).findById(1L);
        }
    }

    @Nested
    @DisplayName("updateCategory")
    class UpdateCategory {
        @Test
        @DisplayName("should update and return category")
        void shouldUpdateCategory() {
            // Arrange
            CategoryUpdateRequest request = new CategoryUpdateRequest("Updated Fantasy", "Updated Desc");
            when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
            when(categoryRepository.save(any(Category.class))).thenReturn(category);

            // Act
            CategoryResponse result = categoryService.updateCategory(1L, request);

            // Assert
            assertThat(result.name()).isEqualTo("Updated Fantasy");
            verify(categoryRepository).findById(1L);
            verify(categoryRepository).save(any(Category.class));
        }

        @Test
        @DisplayName("should throw ResourceNotFoundException when category not found during update")
        void shouldThrowExceptionWhenCategoryNotFoundDuringUpdate() {
            // Arrange
            CategoryUpdateRequest request = new CategoryUpdateRequest("Updated", "Desc");
            when(categoryRepository.findById(1L)).thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> categoryService.updateCategory(1L, request))
                    .isInstanceOf(ResourceNotFoundException.class);

            verify(categoryRepository).findById(1L);
            verify(categoryRepository, never()).save(any(Category.class));
        }
    }

    @Nested
    @DisplayName("deleteCategory")
    class DeleteCategory {
        @Test
        @DisplayName("should delete category when it exists")
        void shouldDeleteCategory() {
            // Arrange
            when(categoryRepository.existsById(1L)).thenReturn(true);

            // Act
            categoryService.deleteCategory(1L);

            // Assert
            verify(categoryRepository).existsById(1L);
            verify(categoryRepository).deleteById(1L);
        }

        @Test
        @DisplayName("should throw ResourceNotFoundException when category not found during deletion")
        void shouldThrowExceptionWhenCategoryNotFoundDuringDeletion() {
            // Arrange
            when(categoryRepository.existsById(1L)).thenReturn(false);

            // Act & Assert
            assertThatThrownBy(() -> categoryService.deleteCategory(1L))
                    .isInstanceOf(ResourceNotFoundException.class);

            verify(categoryRepository).existsById(1L);
            verify(categoryRepository, never()).deleteById(anyLong());
        }
    }
}
