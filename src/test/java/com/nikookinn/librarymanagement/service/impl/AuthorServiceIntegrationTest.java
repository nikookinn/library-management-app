package com.nikookinn.librarymanagement.service.impl;

import com.nikookinn.librarymanagement.dto.request.AuthorCreateRequest;
import com.nikookinn.librarymanagement.dto.request.AuthorUpdateRequest;
import com.nikookinn.librarymanagement.entity.Author;
import com.nikookinn.librarymanagement.repository.AuthorRepository;
import com.nikookinn.librarymanagement.service.AuthorService;
import com.nikookinn.librarymanagement.testsupport.AbstractIntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.CacheManager;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest
@DisplayName("Author Service Integration Tests")
class AuthorServiceIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private AuthorService authorService;

    @MockitoSpyBean
    private AuthorRepository authorRepository;

    @Autowired
    private CacheManager cacheManager;

    @BeforeEach
    void setUp() {
        cacheManager.getCacheNames().forEach(name -> {
            var cache = cacheManager.getCache(name);
            if (cache != null) cache.clear();
        });

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

    @Test
    @DisplayName("should cache author by id and not call repository twice")
    void shouldCacheAuthorById() {
        Author author = new Author();
        author.setFirstName("John");
        author.setLastName("Doe");
        Author saved = authorRepository.save(author);
        reset(authorRepository);

        authorService.getAuthorById(saved.getId());
        authorService.getAuthorById(saved.getId());

        verify(authorRepository, times(1)).findById(saved.getId());
    }

    @Test
    @DisplayName("should cache all authors pageable")
    void shouldCacheGetAllAuthors() {
        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(0, 10);
        
        authorService.getAllAuthors(pageable);
        authorService.getAllAuthors(pageable);

        verify(authorRepository, times(1)).findAll(pageable);
    }

    @Test
    @DisplayName("should evict all entries in authors cache when new author is created")
    void shouldEvictCacheOnCreate() {
        Author author = new Author();
        author.setFirstName("Existing");
        author.setLastName("Author");
        Author saved = authorRepository.save(author);

        authorService.getAuthorById(saved.getId());
        assertThat(cacheManager.getCache("authors").get(saved.getId())).isNotNull();

        authorService.createAuthor(new AuthorCreateRequest("New", "Author", null, "Turk", "Bio"));

        assertThat(cacheManager.getCache("authors").get(saved.getId())).isNull();
    }

    @Test
    @DisplayName("should evict author cache when author is updated")
    void shouldEvictCacheOnUpdate() {
        Author author = new Author();
        author.setFirstName("Old");
        author.setLastName("Name");
        Author saved = authorRepository.save(author);

        authorService.getAuthorById(saved.getId());
        assertThat(cacheManager.getCache("authors").get(saved.getId())).isNotNull();

        authorService.updateAuthor(saved.getId(), new AuthorUpdateRequest("New", "Name", LocalDate.now(), "Bio", "Info"));

        assertThat(cacheManager.getCache("authors").get(saved.getId())).isNull();
    }

    @Test
    @DisplayName("should evict all entries in authors cache when author is deleted")
    void shouldEvictCacheOnDelete() {
        Author author = new Author();
        author.setFirstName("To Be");
        author.setLastName("Deleted");
        Author saved = authorRepository.save(author);

        authorService.getAuthorById(saved.getId());
        assertThat(cacheManager.getCache("authors").get(saved.getId())).isNotNull();

        authorService.deleteAuthor(saved.getId());

        assertThat(cacheManager.getCache("authors").get(saved.getId())).isNull();
    }
}
