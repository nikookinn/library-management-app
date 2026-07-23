package com.nikookinn.librarymanagement.repository;

import com.nikookinn.librarymanagement.entity.Category;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class CategoryRepositoryTest {

    @Autowired
    private CategoryRepository categoryRepository;

    @Test
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
