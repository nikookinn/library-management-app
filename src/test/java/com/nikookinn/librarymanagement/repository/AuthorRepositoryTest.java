package com.nikookinn.librarymanagement.repository;

import com.nikookinn.librarymanagement.entity.Author;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
@DisplayName("Author Repository Integration Tests")
class AuthorRepositoryTest {

    @Autowired
    private AuthorRepository authorRepository;

    private Author author1;
    private Author author2;

    @BeforeEach
    void setUp() {
        author1 = new Author();
        author1.setFirstName("J.R.R.");
        author1.setLastName("Tolkien");
        author1.setBirthDate(LocalDate.of(1892, 1, 3));
        author1.setNationality("British");
        author1.setBiography("Author of The Lord of the Rings");

        author2 = new Author();
        author2.setFirstName("J.K.");
        author2.setLastName("Rowling");
        author2.setBirthDate(LocalDate.of(1965, 7, 31));
        author2.setNationality("British");
        author2.setBiography("Author of Harry Potter");

        authorRepository.save(author1);
        authorRepository.save(author2);
    }

    @Test
    @DisplayName("should find author by full name containing (case-insensitive)")
    void shouldFindAuthorByFullNameContainingIgnoreCase() {
        // Arrange
        String searchTerm = "tolkien";
        Pageable pageable = PageRequest.of(0, 10);

        // Act
        Page<Author> result = authorRepository.findByNameContainingIgnoreCase(searchTerm, pageable);

        // Assert
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().getFirst().getLastName()).isEqualTo("Tolkien");
    }

    @Test
    @DisplayName("should find author by last name containing (case-insensitive)")
    void shouldFindAuthorByLastNameContainingIgnoreCase() {
        // Arrange
        String searchTerm = "rowling";
        Pageable pageable = PageRequest.of(0, 10);

        // Act
        Page<Author> result = authorRepository.findByNameContainingIgnoreCase(searchTerm, pageable);

        // Assert
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().getFirst().getLastName()).isEqualTo("Rowling");
    }

    @Test
    @DisplayName("should return empty page when author not found by name")
    void shouldReturnEmptyPageWhenAuthorNotFoundByName() {
        // Arrange
        String searchTerm = "NonExistent";
        Pageable pageable = PageRequest.of(0, 10);

        // Act
        Page<Author> result = authorRepository.findByNameContainingIgnoreCase(searchTerm, pageable);

        // Assert
        assertThat(result.getContent()).isEmpty();
    }
}

