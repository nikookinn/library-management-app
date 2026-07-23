package com.nikookinn.librarymanagement.repository;

import com.nikookinn.librarymanagement.entity.Author;
import com.nikookinn.librarymanagement.entity.Book;
import com.nikookinn.librarymanagement.entity.Category;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class BookRepositoryTest {

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private AuthorRepository authorRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    private Book book1;
    private Category category;
    private Author author;

    @BeforeEach
    void setUp() {
        author = new Author();
        author.setFirstName("J.R.R.");
        author.setLastName("Tolkien");
        author = authorRepository.save(author);

        category = new Category();
        category.setName("Fantasy");
        category = categoryRepository.save(category);

        book1 = new Book();
        book1.setTitle("The Fellowship of the Ring");
        book1.setIsbn("978-0618640157");
        book1.setTotalCopies(5);
        book1.setAvailableCopies(5);
        book1.setCategory(category);
        Set<Author> authors = new HashSet<>();
        authors.add(author);
        book1.setAuthors(authors);
        book1 = bookRepository.save(book1);

        Book book2 = new Book();
        book2.setTitle("The Hobbit");
        book2.setIsbn("0987654321");
        book2.setTotalCopies(3);
        book2.setAvailableCopies(0);
        book2.setAuthors(authors);
        bookRepository.save(book2);
    }

    @Test
    void shouldFindBooksByCategoryId() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);

        // Act
        Page<Book> result = bookRepository.findByCategory_Id(category.getId(), pageable);

        // Assert
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().getFirst().getTitle()).isEqualTo("The Fellowship of the Ring");
    }

    @Test
    void shouldFindBooksByAuthorId() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);

        // Act
        Page<Book> result = bookRepository.findByAuthor_Id(author.getId(), pageable);

        // Assert
        assertThat(result.getContent()).hasSize(2);
    }

    @Test
    void shouldFindBooksByTitleContainingIgnoreCase() {
        // Arrange
        String titleSearch = "hobb";
        Pageable pageable = PageRequest.of(0, 10);

        // Act
        Page<Book> result = bookRepository.findByTitleContainingIgnoreCase(titleSearch, pageable);

        // Assert
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().getFirst().getTitle()).isEqualTo("The Hobbit");
    }

    @Test
    void shouldFindAvailableBooks() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);

        // Act
        Page<Book> result = bookRepository.findByAvailableCopiesGreaterThan(0, pageable);

        // Assert
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().getFirst().getTitle()).isEqualTo("The Fellowship of the Ring");
    }
}
