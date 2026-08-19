package com.nikookinn.librarymanagement.repository;

import com.nikookinn.librarymanagement.entity.Author;
import com.nikookinn.librarymanagement.entity.Book;
import com.nikookinn.librarymanagement.entity.Category;
import com.nikookinn.librarymanagement.testsupport.AbstractIntegrationTest;
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
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
@DisplayName("Author Repository Integration Tests")
class AuthorRepositoryTest extends AbstractIntegrationTest {

    @Autowired
    private AuthorRepository authorRepository;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    private Author author1;
    private Author author2;
    private Author author3;
    private Category fiction;
    private Category science;

    @BeforeEach
    void setUp() {
        bookRepository.deleteAll();
        authorRepository.deleteAll();
        categoryRepository.deleteAll();

        fiction = new Category();
        fiction.setName("Fiction");
        categoryRepository.save(fiction);

        science = new Category();
        science.setName("Science");
        categoryRepository.save(science);

        author1 = new Author();
        author1.setFirstName("J.R.R.");
        author1.setLastName("Tolkien");
        author1.setBirthDate(LocalDate.of(1892, 1, 3));
        author1.setNationality("British");
        authorRepository.save(author1);

        author2 = new Author();
        author2.setFirstName("J.K.");
        author2.setLastName("Rowling");
        author2.setBirthDate(LocalDate.of(1965, 7, 31));
        author2.setNationality("British");
        authorRepository.save(author2);

        author3 = new Author();
        author3.setFirstName("Isaac");
        author3.setLastName("Asimov");
        author3.setNationality("American");
        authorRepository.save(author3);

        // Tolkien has 1 fiction book
        Book book1 = new Book();
        book1.setTitle("The Hobbit");
        book1.setIsbn("ISBN1");
        book1.setCategory(fiction);
        book1.setAuthors(Set.of(author1));
        book1.setTotalCopies(10);
        book1.setAvailableCopies(10);
        bookRepository.save(book1);

        // Rowling has 2 fiction books
        Book book2 = new Book();
        book2.setTitle("Harry Potter 1");
        book2.setIsbn("ISBN2");
        book2.setCategory(fiction);
        book2.setAuthors(Set.of(author2));
        book2.setTotalCopies(10);
        book2.setAvailableCopies(10);
        bookRepository.save(book2);

        Book book3 = new Book();
        book3.setTitle("Harry Potter 2");
        book3.setIsbn("ISBN3");
        book3.setCategory(fiction);
        book3.setAuthors(Set.of(author2));
        book3.setTotalCopies(10);
        book3.setAvailableCopies(10);
        bookRepository.save(book3);

        // Asimov has 1 fiction and 1 science book (Multi-category author)
        Book book4 = new Book();
        book4.setTitle("Foundation");
        book4.setIsbn("ISBN4");
        book4.setCategory(science);
        book4.setAuthors(Set.of(author3));
        book4.setTotalCopies(10);
        book4.setAvailableCopies(10);
        bookRepository.save(book4);

        Book book5 = new Book();
        book5.setTitle("I, Robot");
        book5.setIsbn("ISBN5");
        book5.setCategory(fiction);
        book5.setAuthors(Set.of(author3));
        book5.setTotalCopies(10);
        book5.setAvailableCopies(10);
        bookRepository.save(book5);
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

    @Test
    @DisplayName("should find authors who have at least one book")
    void shouldFindAuthorsWithBooks() {
        // Act
        List<Author> result = authorRepository.findAuthorsWithBooks();

        // Assert
        // All three authors in setup have books
        assertThat(result).hasSize(3);
        assertThat(result).extracting(Author::getLastName)
                .containsExactlyInAnyOrder("Tolkien", "Rowling", "Asimov");
    }

    @Test
    @DisplayName("should find prolific authors with minimum number of books")
    void shouldFindProlificAuthors() {
        // Act
        // Rowling has 2, Asimov has 2, Tolkien has 1.
        List<Object[]> result = authorRepository.findProlificsAuthors(2);

        // Assert
        assertThat(result).hasSize(2);
        assertThat(result.getFirst()[2]).isIn("Rowling", "Asimov");
        assertThat(((Number) result.getFirst()[3]).intValue()).isEqualTo(2);
    }

    @Test
    @DisplayName("should find authors by category name")
    void shouldFindAuthorsByCategoryName() {
        // Act
        List<Author> result = authorRepository.findAuthorsByCategoryName("Science");

        // Assert
        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getLastName()).isEqualTo("Asimov");
    }

    @Test
    @DisplayName("should find authors who write in multiple categories")
    void shouldFindMultiCategoryAuthors() {
        // Act
        // Asimov has books in both Fiction and Science
        List<Author> result = authorRepository.findMultiCategoryAuthors();

        // Assert
        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getLastName()).isEqualTo("Asimov");
    }
}

