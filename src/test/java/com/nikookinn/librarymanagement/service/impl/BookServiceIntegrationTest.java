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
import com.nikookinn.librarymanagement.testsupport.AbstractIntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.CacheManager;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest
@DisplayName("Book Service Integration Tests")
class BookServiceIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private BookService bookService;

    @MockitoSpyBean
    private BookRepository bookRepository;

    @MockitoSpyBean
    private CategoryRepository categoryRepository;
    
    @MockitoSpyBean
    private AuthorRepository authorRepository;

    @Autowired
    private CacheManager cacheManager;

    @Autowired
    private jakarta.persistence.EntityManager entityManager;

    private Category category;
    private Book book;

    @BeforeEach
    void setUp() {
        cacheManager.getCacheNames().forEach(name -> {
            var cache = cacheManager.getCache(name);
            if (cache != null) cache.clear();
        });

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

    @Test
    @DisplayName("should cache book by id and not call repository twice")
    void shouldCacheBookById() {
        // First call - should hit repository
        bookService.getBookById(book.getId());

        // Second call - should hit cache
        bookService.getBookById(book.getId());

        // Verify repository was called exactly once for findById
        verify(bookRepository, times(1)).findById(book.getId());
    }

    @Test
    @DisplayName("should cache all books pageable")
    void shouldCacheGetAllBooks() {
        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(0, 10);
        
        bookService.getAllBooks(pageable);
        bookService.getAllBooks(pageable);

        verify(bookRepository, times(1)).findAll(pageable);
    }

    @Test
    @DisplayName("should evict all entries in books cache when new book is created")
    void shouldEvictCacheOnCreate() {
        // Populating cache
        bookService.getBookById(book.getId());
        assertThat(cacheManager.getCache("books").get(book.getId())).isNotNull();

        BookCreateRequest request = new BookCreateRequest("Fresh Book", "FreshISBN", 2024, "Desc", 3, category.getId());
        bookService.createBook(request);

        // All entries should be gone, including our previously cached book
        assertThat(cacheManager.getCache("books").get(book.getId())).isNull();
    }

    @Test
    @DisplayName("should evict all entries in books cache when book is updated")
    void shouldEvictCacheOnUpdate() {
        bookService.getBookById(book.getId());
        assertThat(cacheManager.getCache("books").get(book.getId())).isNotNull();

        BookUpdateRequest request = new BookUpdateRequest("Updated", "ISBN1", 2020, "D", 10, category.getId());
        bookService.updateBook(book.getId(), request);

        assertThat(cacheManager.getCache("books").get(book.getId())).isNull();
    }

    @Test
    @DisplayName("should evict book cache when book is deleted")
    void shouldEvictCacheOnDelete() {
        // Fill cache
        bookService.getBookById(book.getId());
        assertThat(cacheManager.getCache("books").get(book.getId())).isNotNull();

        // Delete book - should evict
        bookService.deleteBook(book.getId());

        // Verify cache is empty
        assertThat(cacheManager.getCache("books").get(book.getId())).isNull();
    }

    @Test
    @DisplayName("should cache search results")
    void shouldCacheSearchResults() {
        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(0, 10);
        String query = "Original";

        bookService.searchBooks(query, pageable);
        bookService.searchBooks(query, pageable);

        verify(bookRepository, times(1)).findByTitleContainingIgnoreCase(query, pageable);
    }

    @Test
    @DisplayName("should cache top categories statistics")
    void shouldCacheTopCategories() {
        bookService.getTopCategories();
        bookService.getTopCategories();

        verify(bookRepository, times(1)).findTopCategoriesByLoans();
    }

    @Test
    @DisplayName("should cache most borrowed books report")
    void shouldCacheMostBorrowedBooks() {
        int limit = 5;
        bookService.getMostBorrowedBooks(limit);
        bookService.getMostBorrowedBooks(limit);

        verify(bookRepository, times(1)).findMostBorrowedBooks(limit);
    }

    @Test
    @DisplayName("should cache books by category")
    void shouldCacheGetBooksByCategory() {
        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(0, 10);
        bookService.getBooksByCategory(category.getId(), pageable);
        bookService.getBooksByCategory(category.getId(), pageable);

        verify(bookRepository, times(1)).findByCategory_Id(category.getId(), pageable);
    }

    @Test
    @DisplayName("should cache available books")
    void shouldCacheGetAvailableBooks() {
        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(0, 10);
        bookService.getAvailableBooks(pageable);
        bookService.getAvailableBooks(pageable);

        verify(bookRepository, times(1)).findByAvailableCopiesGreaterThan(0, pageable);
    }

    @Test
    @DisplayName("should evict cache when author is removed from book")
    void shouldEvictCacheOnRemoveAuthor() {
        Author author = new Author();
        author.setFirstName("To Remove");
        author.setLastName("Author");
        Author savedAuthor = authorRepository.save(author);
        bookService.addAuthorToBook(book.getId(), savedAuthor.getId());
        
        bookService.getBookById(book.getId());
        assertThat(cacheManager.getCache("books").get(book.getId())).isNotNull();

        bookService.removeAuthorFromBook(book.getId(), savedAuthor.getId());

        assertThat(cacheManager.getCache("books").get(book.getId())).isNull();
    }

    @Test
    @DisplayName("should evict cache when author is added to book")
    void shouldEvictCacheOnAddAuthor() {
        Author author = new Author();
        author.setFirstName("New");
        author.setLastName("Author");
        Author savedAuthor = authorRepository.save(author);

        bookService.getBookById(book.getId());
        assertThat(cacheManager.getCache("books").get(book.getId())).isNotNull();

        bookService.addAuthorToBook(book.getId(), savedAuthor.getId());

        assertThat(cacheManager.getCache("books").get(book.getId())).isNull();
    }
    @Test
    @DisplayName("should cache books by author")
    void shouldCacheGetBooksByAuthor() {
        Author author = new Author();
        author.setFirstName("Writer");
        author.setLastName("One");
        Author savedAuthor = authorRepository.save(author);

        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(0, 10);
        bookService.getBooksByAuthor(savedAuthor.getId(), pageable);
        bookService.getBooksByAuthor(savedAuthor.getId(), pageable);

        verify(authorRepository, times(1)).findById(savedAuthor.getId());
        verify(bookRepository, times(1)).findByAuthor_Id(savedAuthor.getId(), pageable);
    }

    @Test
    @DisplayName("should cache dynamic search results")
    void shouldCacheSearchBooksDynamic() {
        com.nikookinn.librarymanagement.dto.request.BookSearchRequest request = 
            new com.nikookinn.librarymanagement.dto.request.BookSearchRequest("Original", null, null, null, null, null);
        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(0, 10);

        bookService.searchBooksDynamic(request, pageable);
        bookService.searchBooksDynamic(request, pageable);

        verify(bookRepository, times(1)).findAll(any(org.springframework.data.jpa.domain.Specification.class), eq(pageable));
    }

    @Test
    @DisplayName("should cache available books with details")
    void shouldCacheGetAvailableBooksWithDetails() {
        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(0, 10);
        bookService.getAvailableBooksWithDetails(pageable);
        bookService.getAvailableBooksWithDetails(pageable);

        verify(bookRepository, times(1)).findAvailableBooksWithDetails(pageable);
    }

    @Test
    @DisplayName("should cache books by category and availability")
    void shouldCacheGetBooksByCategoryAndAvailability() {
        bookService.getBooksByCategoryAndAvailability(category.getId(), 1);
        bookService.getBooksByCategoryAndAvailability(category.getId(), 1);

        verify(bookRepository, times(1)).findByCategoryAndAvailability(category.getId(), 1);
    }

    @Test
    @DisplayName("should cache books never borrowed")
    void shouldCacheGetBooksNeverBorrowed() {
        bookService.getBooksNeverBorrowed();
        bookService.getBooksNeverBorrowed();

        verify(bookRepository, times(1)).findBooksNeverBorrowed();
    }

    @Test
    @DisplayName("should upload cover image and evict cache")
    void shouldUploadCoverAndEvictCache() {
        MockMultipartFile file = new MockMultipartFile(
                "file", "cover.jpg", "image/jpeg", "content".getBytes());

        bookService.getBookById(book.getId());
        assertThat(cacheManager.getCache("books").get(book.getId())).isNotNull();

        bookService.uploadCoverImage(book.getId(), file);

        assertThat(cacheManager.getCache("books").get(book.getId())).isNull();
        Book updatedBook = bookRepository.findById(book.getId()).orElseThrow();
        assertThat(updatedBook.getCoverImage()).isNotNull();
    }

    @Test
    @DisplayName("should fail to upload file larger than 5MB")
    void shouldFailLargeFileUpload() {
        byte[] largeContent = new byte[5 * 1024 * 1024 + 1];
        MockMultipartFile file = new MockMultipartFile(
                "file", "large.jpg", "image/jpeg", largeContent);

        assertThatThrownBy(() -> bookService.uploadCoverImage(book.getId(), file))
                .isInstanceOf(com.nikookinn.librarymanagement.exception.BusinessRuleViolationException.class)
                .hasMessage("File size exceeds 5MB limit");
    }

    @Test
    @DisplayName("should delete cover image successfully")
    void shouldDeleteCoverImageSuccessfully() {
        MockMultipartFile file = new MockMultipartFile(
                "file", "cover.jpg", "image/jpeg", "content".getBytes());
        bookService.uploadCoverImage(book.getId(), file);
        
        bookService.deleteCoverImage(book.getId());

        Book updatedBook = bookRepository.findById(book.getId()).orElseThrow();
        assertThat(updatedBook.getCoverImage()).isNull();
    }

    @Test
    @DisplayName("should overwrite existing cover image successfully")
    void shouldOverwriteExistingCover() {
        MockMultipartFile file1 = new MockMultipartFile(
                "file", "cover1.jpg", "image/jpeg", "content1".getBytes());
        bookService.uploadCoverImage(book.getId(), file1);
        Book bookWithFirstCover = bookRepository.findById(book.getId()).orElseThrow();
        String firstCoverName = bookWithFirstCover.getCoverImage();

        MockMultipartFile file2 = new MockMultipartFile(
                "file", "cover2.jpg", "image/jpeg", "content2".getBytes());
        bookService.uploadCoverImage(book.getId(), file2);

        Book bookWithSecondCover = bookRepository.findById(book.getId()).orElseThrow();
        assertThat(bookWithSecondCover.getCoverImage()).isNotEqualTo(firstCoverName);
        assertThat(bookService.getCoverImage(book.getId())).isEqualTo("content2".getBytes());
    }
}
