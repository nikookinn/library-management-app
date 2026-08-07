package com.nikookinn.librarymanagement.service.impl;

import com.nikookinn.librarymanagement.dto.request.BookCreateRequest;
import com.nikookinn.librarymanagement.dto.request.BookUpdateRequest;
import com.nikookinn.librarymanagement.entity.Author;
import com.nikookinn.librarymanagement.entity.Book;
import com.nikookinn.librarymanagement.entity.Category;
import com.nikookinn.librarymanagement.repository.AuthorRepository;
import com.nikookinn.librarymanagement.repository.BookRepository;
import com.nikookinn.librarymanagement.repository.CategoryRepository;
import com.nikookinn.librarymanagement.service.BookService;
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
@DisplayName("Book Service Integration Tests")
class BookServiceIntegrationTest {

    @Autowired
    private BookService bookService;

    @MockitoSpyBean
    private BookRepository bookRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private AuthorRepository authorRepository;

    @Autowired
    private jakarta.persistence.EntityManager entityManager;

    private Category category;
    private Book book;

    @BeforeEach
    void setUp() {
        bookRepository.deleteAll();
        categoryRepository.deleteAll();
        authorRepository.deleteAll();

        category = new Category();
        category.setName("Fiction");
        category = categoryRepository.save(category);

        book = new Book();
        book.setTitle("Original Title");
        book.setIsbn("1234567890");
        book.setTotalCopies(5);
        book.setAvailableCopies(5);
        book.setCategory(category);
        book = bookRepository.save(book);
    }

    @Test
    @DisplayName("should rollback book creation when save fails")
    void shouldRollbackBookCreationWhenSaveFails() {
        BookCreateRequest request = new BookCreateRequest("New Book", "ISBN999", 2023, "Desc", 5, category.getId());

        doThrow(new RuntimeException("Save failed")).when(bookRepository).save(any(Book.class));

        assertThatThrownBy(() -> bookService.createBook(request))
                .isInstanceOf(RuntimeException.class);

        // Only original book remains
        assertThat(bookRepository.count()).isEqualTo(1);
    }

    @Test
    @DisplayName("should rollback book update when save fails")
    void shouldRollbackBookUpdateWhenSaveFails() {
        BookUpdateRequest request = new BookUpdateRequest("Updated Title", "ISBN123", 2020, "New Desc", 10, category.getId());

        doThrow(new RuntimeException("Update failed")).when(bookRepository).save(any(Book.class));

        assertThatThrownBy(() -> bookService.updateBook(book.getId(), request))
                .isInstanceOf(RuntimeException.class);

        Book notUpdated = bookRepository.findById(book.getId()).orElseThrow();
        assertThat(notUpdated.getTitle()).isEqualTo("Original Title");
    }

    @Test
    @DisplayName("should rollback book-author association when book save fails in addAuthorToBook")
    void shouldRollbackAuthorAssociationWhenBookSaveFails() {
        Author author = new Author();
        author.setFirstName("J.R.R.");
        author.setLastName("Tolkien");
        Author savedAuthor = authorRepository.save(author);

        doThrow(new RuntimeException("Simulated error")).when(bookRepository).save(any(Book.class));

        assertThatThrownBy(() -> bookService.addAuthorToBook(book.getId(), savedAuthor.getId()))
                .isInstanceOf(RuntimeException.class);

        Number count = (Number) entityManager.createNativeQuery(
                "SELECT COUNT(*) FROM book_authors WHERE book_id = ?1")
                .setParameter(1, book.getId())
                .getSingleResult();
        assertThat(count.intValue()).isZero();
    }
}
