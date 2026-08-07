package com.nikookinn.librarymanagement.service.impl;

import com.nikookinn.librarymanagement.dto.request.AuthorCreateRequest;
import com.nikookinn.librarymanagement.dto.request.AuthorUpdateRequest;
import com.nikookinn.librarymanagement.entity.Author;
import com.nikookinn.librarymanagement.repository.AuthorRepository;
import com.nikookinn.librarymanagement.service.AuthorService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;

@SpringBootTest
@DisplayName("Author Service Integration Tests")
class AuthorServiceIntegrationTest {

    @Autowired
    private AuthorService authorService;

    @MockitoSpyBean
    private AuthorRepository authorRepository;

    @BeforeEach
    void setUp() {
        authorRepository.deleteAll();
    }

    @Test
    @DisplayName("should rollback author creation when save fails")
    void shouldRollbackAuthorCreationWhenSaveFails() {
        AuthorCreateRequest request = new AuthorCreateRequest("J.R.R.", "Tolkien", LocalDate.of(1892, 1, 3), "British", "Bio");
        
        doThrow(new RuntimeException("Simulated error")).when(authorRepository).save(any(Author.class));

        assertThatThrownBy(() -> authorService.createAuthor(request))
                .isInstanceOf(RuntimeException.class);

        assertThat(authorRepository.count()).isZero();
    }

    @Test
    @DisplayName("should rollback author update when save fails")
    void shouldRollbackAuthorUpdateWhenSaveFails() {
        Author author = new Author();
        author.setFirstName("Original");
        author.setLastName("Author");
        final Author savedAuthor = authorRepository.save(author);

        AuthorUpdateRequest request = new AuthorUpdateRequest("Updated", "Name", LocalDate.now(), "Unknown", "New Bio");

        doThrow(new RuntimeException("Simulated error")).when(authorRepository).save(any(Author.class));

        assertThatThrownBy(() -> authorService.updateAuthor(savedAuthor.getId(), request))
                .isInstanceOf(RuntimeException.class);

        Author notUpdated = authorRepository.findById(savedAuthor.getId()).orElseThrow();
        assertThat(notUpdated.getFirstName()).isEqualTo("Original");
    }

    @Test
    @DisplayName("should rollback author deletion when something fails after delete check")
    void shouldRollbackAuthorDeletionWhenDeleteFails() {
        Author author = new Author();
        author.setFirstName("To Be Deleted");
        author.setLastName("Author");
        final Author authorToDelete = authorRepository.save(author);

        doThrow(new RuntimeException("Simulated error during delete")).when(authorRepository).deleteById(any(Long.class));

        assertThatThrownBy(() -> authorService.deleteAuthor(authorToDelete.getId()))
                .isInstanceOf(RuntimeException.class);

        assertThat(authorRepository.existsById(authorToDelete.getId())).isTrue();
    }
}
