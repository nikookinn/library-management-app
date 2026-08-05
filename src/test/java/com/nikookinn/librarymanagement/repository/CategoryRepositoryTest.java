package com.nikookinn.librarymanagement.repository;

import com.nikookinn.librarymanagement.entity.Category;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
@DisplayName("Category Repository Integration Tests")
class CategoryRepositoryTest {

    @Autowired
    private CategoryRepository categoryRepository;

    @Test
    @DisplayName("should save and find category by ID")
    void shouldSaveAndFindCategoryById() {
        // Arrange
        Category category = new Category();
        category.setName("Fantasy");

        // Act
        Category savedCategory = categoryRepository.save(category);
        Optional<Category> foundCategory = categoryRepository.findById(savedCategory.getId());

        // Assert
        assertThat(foundCategory).isPresent();
        assertThat(foundCategory.get().getName()).isEqualTo("Fantasy");
    }

    @Test
    @DisplayName("should delete category")
    void shouldDeleteCategory() {
        // Arrange
        Category category = new Category();
        category.setName("Adventure");
        Category savedCategory = categoryRepository.save(category);

        // Act
        categoryRepository.deleteById(savedCategory.getId());
        Optional<Category> foundCategory = categoryRepository.findById(savedCategory.getId());

        // Assert
        assertThat(foundCategory).isNotPresent();
    }
}

