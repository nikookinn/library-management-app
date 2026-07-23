package com.nikookinn.librarymanagement.service.impl;

import com.nikookinn.librarymanagement.dto.request.AuthorCreateRequest;
import com.nikookinn.librarymanagement.dto.request.AuthorUpdateRequest;
import com.nikookinn.librarymanagement.dto.response.AuthorResponse;
import com.nikookinn.librarymanagement.entity.Author;
import com.nikookinn.librarymanagement.exception.ResourceNotFoundException;
import com.nikookinn.librarymanagement.repository.AuthorRepository;
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

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Author Service Unit Tests")
class AuthorServiceImplTest {

    @Mock
    private AuthorRepository authorRepository;

    @InjectMocks
    private AuthorServiceImpl authorService;

    private Author author;

    @BeforeEach
    void setUp() {
        author = new Author();
        author.setId(1L);
        author.setFirstName("J.R.R.");
        author.setLastName("Tolkien");
        author.setBirthDate(LocalDate.of(1892, 1, 3));
        author.setNationality("British");
        author.setBiography("Author of The Lord of the Rings");
    }

    @Nested
    @DisplayName("getAllAuthors")
    class GetAllAuthors {
        @Test
        @DisplayName("should return all authors with pagination")
        void shouldGetAllAuthors() {
            // Arrange
            Pageable pageable = PageRequest.of(0, 10);
            Page<Author> authorPage = new PageImpl<>(List.of(author));
            when(authorRepository.findAll(pageable)).thenReturn(authorPage);

            // Act
            Page<AuthorResponse> result = authorService.getAllAuthors(pageable);

            // Assert
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).firstName()).isEqualTo("J.R.R.");
            verify(authorRepository).findAll(pageable);
        }
    }

    @Nested
    @DisplayName("getAuthorById")
    class GetAuthorById {
        @Test
        @DisplayName("should return author when valid ID is provided")
        void shouldGetAuthorById() {
            // Arrange
            when(authorRepository.findById(1L)).thenReturn(Optional.of(author));

            // Act
            AuthorResponse result = authorService.getAuthorById(1L);

            // Assert
            assertThat(result.firstName()).isEqualTo("J.R.R.");
            verify(authorRepository).findById(1L);
        }

        @Test
        @DisplayName("should throw ResourceNotFoundException when author not found")
        void shouldThrowExceptionWhenAuthorNotFoundById() {
            // Arrange
            when(authorRepository.findById(1L)).thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> authorService.getAuthorById(1L))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Author not found");
        }
    }

    @Nested
    @DisplayName("createAuthor")
    class CreateAuthor {
        @Test
        @DisplayName("should create and return author")
        void shouldCreateAuthor() {
            // Arrange
            AuthorCreateRequest request = new AuthorCreateRequest("J.R.R.", "Tolkien", LocalDate.of(1892, 1, 3), "British", "Author of The Lord of the Rings");
            when(authorRepository.save(any(Author.class))).thenReturn(author);

            // Act
            AuthorResponse result = authorService.createAuthor(request);

            // Assert
            assertThat(result.firstName()).isEqualTo("J.R.R.");
            verify(authorRepository).save(any(Author.class));
        }
    }

    @Nested
    @DisplayName("updateAuthor")
    class UpdateAuthor {
        @Test
        @DisplayName("should update and return author")
        void shouldUpdateAuthor() {
            // Arrange
            AuthorUpdateRequest request = new AuthorUpdateRequest("J.K.", "Rowling", LocalDate.of(1965, 7, 31), "British", "Author of Harry Potter");
            author.setFirstName("J.K.");
            author.setLastName("Rowling");
            when(authorRepository.findById(1L)).thenReturn(Optional.of(author));
            when(authorRepository.save(any(Author.class))).thenReturn(author);

            // Act
            AuthorResponse result = authorService.updateAuthor(1L, request);

            // Assert
            assertThat(result.firstName()).isEqualTo("J.K.");
            verify(authorRepository).findById(1L);
            verify(authorRepository).save(any(Author.class));
        }

        @Test
        @DisplayName("should throw ResourceNotFoundException when author not found during update")
        void shouldThrowExceptionWhenAuthorNotFoundDuringUpdate() {
            // Arrange
            AuthorUpdateRequest request = new AuthorUpdateRequest("Updated", "Name", LocalDate.now(), "Nationality", "Bio");
            when(authorRepository.findById(1L)).thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> authorService.updateAuthor(1L, request))
                    .isInstanceOf(ResourceNotFoundException.class);

            verify(authorRepository).findById(1L);
            verify(authorRepository, never()).save(any(Author.class));
        }
    }

    @Nested
    @DisplayName("deleteAuthor")
    class DeleteAuthor {
        @Test
        @DisplayName("should delete author when author exists")
        void shouldDeleteAuthor() {
            // Arrange
            when(authorRepository.existsById(1L)).thenReturn(true);

            // Act
            authorService.deleteAuthor(1L);

            // Assert
            verify(authorRepository).deleteById(1L);
        }

        @Test
        @DisplayName("should throw ResourceNotFoundException when author does not exist")
        void shouldThrowExceptionWhenDeletingNonExistentAuthor() {
            // Arrange
            when(authorRepository.existsById(1L)).thenReturn(false);

            // Act & Assert
            assertThatThrownBy(() -> authorService.deleteAuthor(1L))
                    .isInstanceOf(ResourceNotFoundException.class);
            verify(authorRepository, never()).deleteById(any());
        }
    }

    @Nested
    @DisplayName("searchAuthors")
    class SearchAuthors {
        @Test
        @DisplayName("should return authors matching search criteria")
        void shouldSearchAuthors() {
            // Arrange
            String name = "Tolkien";
            Pageable pageable = PageRequest.of(0, 10);
            Page<Author> authorPage = new PageImpl<>(List.of(author));
            when(authorRepository.findByNameContainingIgnoreCase(name, pageable)).thenReturn(authorPage);

            // Act
            Page<AuthorResponse> result = authorService.searchAuthors(name, pageable);

            // Assert
            assertThat(result.getContent()).hasSize(1);
            verify(authorRepository).findByNameContainingIgnoreCase(name, pageable);
        }
    }
}
