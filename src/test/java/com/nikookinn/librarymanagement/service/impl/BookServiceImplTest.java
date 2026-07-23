package com.nikookinn.librarymanagement.service.impl;

import com.nikookinn.librarymanagement.dto.request.BookCreateRequest;
import com.nikookinn.librarymanagement.dto.request.BookUpdateRequest;
import com.nikookinn.librarymanagement.dto.response.BookResponse;
import com.nikookinn.librarymanagement.entity.Author;
import com.nikookinn.librarymanagement.entity.Book;
import com.nikookinn.librarymanagement.entity.Category;
import com.nikookinn.librarymanagement.entity.LoanStatus;
import com.nikookinn.librarymanagement.exception.BusinessRuleViolationException;
import com.nikookinn.librarymanagement.exception.ResourceNotFoundException;
import com.nikookinn.librarymanagement.repository.AuthorRepository;
import com.nikookinn.librarymanagement.repository.BookRepository;
import com.nikookinn.librarymanagement.repository.CategoryRepository;
import com.nikookinn.librarymanagement.repository.LoanRepository;
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

import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Book Service Unit Tests")
class BookServiceImplTest {

    @Mock
    private BookRepository bookRepository;
    @Mock
    private CategoryRepository categoryRepository;
    @Mock
    private AuthorRepository authorRepository;
    @Mock
    private LoanRepository loanRepository;

    @InjectMocks
    private BookServiceImpl bookService;

    private Book book;
    private Category category;
    private Author author;

    @BeforeEach
    void setUp() {
        category = new Category();
        category.setId(1L);
        category.setName("Fantasy");

        author = new Author();
        author.setId(1L);
        author.setFirstName("J.R.R.");
        author.setLastName("Tolkien");

        book = new Book();
        book.setId(1L);
        book.setTitle("The Fellowship of the Ring");
        book.setIsbn("978-0618640157");
        book.setTotalCopies(5);
        book.setAvailableCopies(5);
        book.setCategory(category);
        book.setAuthors(new HashSet<>(List.of(author)));
    }

    @Nested
    @DisplayName("getAllBooks")
    class GetAllBooks {
        @Test
        @DisplayName("should return all books with pagination")
        void shouldGetAllBooks() {
            // Arrange
            Pageable pageable = PageRequest.of(0, 10);
            Page<Book> bookPage = new PageImpl<>(List.of(book));
            when(bookRepository.findAll(pageable)).thenReturn(bookPage);

            // Act
            Page<BookResponse> result = bookService.getAllBooks(pageable);

            // Assert
            assertThat(result.getContent()).hasSize(1);
            verify(bookRepository).findAll(pageable);
        }
    }

    @Nested
    @DisplayName("createBook")
    class CreateBook {
        @Test
        @DisplayName("should throw ResourceNotFoundException when category does not exist")
        void shouldThrowExceptionWhenCategoryNotFound() {
            // Arrange
            BookCreateRequest request = new BookCreateRequest("The Fellowship of the Ring", "978-0618640157", 1954, "Epic fantasy novel", 5, 1L);
            when(categoryRepository.findById(1L)).thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> bookService.createBook(request))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Category not found");

            verify(categoryRepository).findById(1L);
            verify(bookRepository, never()).save(any(Book.class));
        }
    }

    @Nested
    @DisplayName("updateBook")
    class UpdateBook {
        @Test
        @DisplayName("should update book when valid data is provided")
        void shouldUpdateBook() {
            BookUpdateRequest request = new BookUpdateRequest("The Return of the King", "978-0618640157", 1955, "Final part of LOTR", 10, 1L);
            book.setTitle("The Return of the King");
            when(bookRepository.findById(1L)).thenReturn(Optional.of(book));
            when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
            when(loanRepository.countByBook_IdAndStatusIn(anyLong(), any())).thenReturn(2L);
            when(bookRepository.save(any(Book.class))).thenReturn(book);

            // Act
            BookResponse result = bookService.updateBook(1L, request);

            // Assert
            verify(bookRepository).findById(1L);
            verify(loanRepository).countByBook_IdAndStatusIn(eq(1L), any());
            verify(categoryRepository).findById(1L);
            verify(bookRepository).save(any(Book.class));
            assertThat(result.title()).isEqualTo("The Return of the King");
        }

        @Test
        @DisplayName("should throw ResourceNotFoundException when book not found")
        void shouldThrowExceptionWhenBookNotFound() {
            // Arrange
            BookUpdateRequest request = new BookUpdateRequest("Updated", "123456", 2024, "Desc", 10, 1L);
            when(bookRepository.findById(1L)).thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> bookService.updateBook(1L, request))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Book not found");

            verify(bookRepository).findById(1L);
            verify(loanRepository, never()).countByBook_IdAndStatusIn(anyLong(), any());
            verify(categoryRepository, never()).findById(anyLong());
            verify(bookRepository, never()).save(any(Book.class));
        }

        @Test
        @DisplayName("should throw BusinessRuleViolationException when total copies is less than borrowed")
        void shouldThrowExceptionWhenUpdateBookWithFewerTotalCopiesThanBorrowed() {
            // Arrange
            BookUpdateRequest request = new BookUpdateRequest("Updated", "123456", 2024, "Desc", 1, 1L);
            when(bookRepository.findById(1L)).thenReturn(Optional.of(book));
            when(loanRepository.countByBook_IdAndStatusIn(anyLong(), any())).thenReturn(2L);

            // Act & Assert
            assertThatThrownBy(() -> bookService.updateBook(1L, request))
                    .isInstanceOf(BusinessRuleViolationException.class)
                    .hasMessageContaining("Total copies cannot be less than");

            verify(bookRepository).findById(1L);
            verify(loanRepository).countByBook_IdAndStatusIn(eq(1L), any());
            verify(categoryRepository, never()).findById(anyLong());
            verify(bookRepository, never()).save(any(Book.class));
        }

        @Test
        @DisplayName("should throw ResourceNotFoundException when category not found during update")
        void shouldThrowExceptionWhenCategoryNotFoundDuringUpdate() {
            // Arrange
            BookUpdateRequest request = new BookUpdateRequest("Updated", "123456", 2024, "Desc", 10, 1L);
            when(bookRepository.findById(1L)).thenReturn(Optional.of(book));
            when(loanRepository.countByBook_IdAndStatusIn(anyLong(), any())).thenReturn(2L);
            when(categoryRepository.findById(1L)).thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> bookService.updateBook(1L, request))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Category not found");

            verify(bookRepository).findById(1L);
            verify(loanRepository).countByBook_IdAndStatusIn(eq(1L), any());
            verify(categoryRepository).findById(1L);
            verify(bookRepository, never()).save(any(Book.class));
        }
    }

    @Nested
    @DisplayName("addAuthorToBook")
    class AddAuthorToBook {
        @Test
        @DisplayName("should add author to book successfully")
        void shouldAddAuthorToBook() {
            // Arrange
            when(bookRepository.findById(1L)).thenReturn(Optional.of(book));
            when(authorRepository.findById(1L)).thenReturn(Optional.of(author));

            // Act
            bookService.addAuthorToBook(1L, 1L);

            // Assert
            verify(bookRepository).findById(1L);
            verify(authorRepository).findById(1L);
            verify(bookRepository).save(book);
        }

        @Test
        @DisplayName("should throw ResourceNotFoundException when book not found")
        void shouldThrowExceptionWhenBookNotFound() {
            // Arrange
            when(bookRepository.findById(1L)).thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> bookService.addAuthorToBook(1L, 1L))
                    .isInstanceOf(ResourceNotFoundException.class);

            verify(bookRepository).findById(1L);
            verify(authorRepository, never()).findById(anyLong());
            verify(bookRepository, never()).save(any(Book.class));
        }

        @Test
        @DisplayName("should throw ResourceNotFoundException when author not found")
        void shouldThrowExceptionWhenAuthorNotFound() {
            // Arrange
            when(bookRepository.findById(1L)).thenReturn(Optional.of(book));
            when(authorRepository.findById(1L)).thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> bookService.addAuthorToBook(1L, 1L))
                    .isInstanceOf(ResourceNotFoundException.class);

            verify(bookRepository).findById(1L);
            verify(authorRepository).findById(1L);
            verify(bookRepository, never()).save(any(Book.class));
        }
    }
}
