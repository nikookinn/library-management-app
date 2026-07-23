package com.nikookinn.librarymanagement.service;

import com.nikookinn.librarymanagement.dto.request.BookCreateRequest;
import com.nikookinn.librarymanagement.dto.response.BookResponse;
import com.nikookinn.librarymanagement.dto.request.BookUpdateRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface BookService {
    Page<BookResponse> getAllBooks(Pageable pageable);
    BookResponse getBookById(Long id);
    BookResponse createBook(BookCreateRequest request);
    BookResponse updateBook(Long id, BookUpdateRequest request);
    void deleteBook(Long id);
    
    Page<BookResponse> getBooksByCategory(Long categoryId, Pageable pageable);
    Page<BookResponse> getBooksByAuthor(Long authorId, Pageable pageable);
    Page<BookResponse> searchBooks(String query, Pageable pageable);
    Page<BookResponse> getAvailableBooks(Pageable pageable);
    
    void addAuthorToBook(Long bookId, Long authorId);
    void removeAuthorFromBook(Long bookId, Long authorId);
}
