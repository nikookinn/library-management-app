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
import org.springframework.cache.CacheManager;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest
@DisplayName("Category Service Integration Tests")
class CategoryServiceIntegrationTest {

    @Autowired
    private CategoryService categoryService;

    @MockitoSpyBean
    private CategoryRepository categoryRepository;

    @Autowired
    private CacheManager cacheManager;

    @BeforeEach
    void setUp() {
        cacheManager.getCacheNames().forEach(name -> {
            var cache = cacheManager.getCache(name);
            if (cache != null) cache.clear();
        });

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

    @Test
    @DisplayName("should cache category by id and not call repository twice")
    void shouldCacheCategoryById() {
        Category category = new Category();
        category.setName("History");
        Category saved = categoryRepository.save(category);
        reset(categoryRepository);

        categoryService.getCategoryById(saved.getId());
        categoryService.getCategoryById(saved.getId());

        verify(categoryRepository, times(1)).findById(saved.getId());
    }

    @Test
    @DisplayName("should cache all categories pageable")
    void shouldCacheGetAllCategories() {
        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(0, 10);
        
        categoryService.getAllCategories(pageable);
        categoryService.getAllCategories(pageable);

        verify(categoryRepository, times(1)).findAll(pageable);
    }

    @Test
    @DisplayName("should evict both category and book caches when category is updated")
    void shouldEvictCachesOnUpdate() {
        Category category = new Category();
        category.setName("Old Category");
        Category saved = categoryRepository.save(category);

        // Fill caches
        categoryService.getCategoryById(saved.getId());
        cacheManager.getCache("books").put(1L, "Some Book");

        assertThat(cacheManager.getCache("categories").get(saved.getId())).isNotNull();
        assertThat(cacheManager.getCache("books").get(1L)).isNotNull();

        categoryService.updateCategory(saved.getId(), new CategoryUpdateRequest("New Category", "Desc"));

        assertThat(cacheManager.getCache("categories").get(saved.getId())).isNull();
        assertThat(cacheManager.getCache("books").get(1L)).isNull();
    }

    @Test
    @DisplayName("should evict category cache when category is deleted")
    void shouldEvictCacheOnDelete() {
        Category category = new Category();
        category.setName("To Delete");
        Category saved = categoryRepository.save(category);

        categoryService.getCategoryById(saved.getId());
        assertThat(cacheManager.getCache("categories").get(saved.getId())).isNotNull();

        categoryService.deleteCategory(saved.getId());

        assertThat(cacheManager.getCache("categories").get(saved.getId())).isNull();
    }
}
