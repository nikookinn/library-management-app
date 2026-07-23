package com.nikookinn.librarymanagement.controller;

import com.nikookinn.librarymanagement.controller.api.BookApi;
import com.nikookinn.librarymanagement.dto.request.BookCreateRequest;
import com.nikookinn.librarymanagement.dto.response.BookResponse;
import com.nikookinn.librarymanagement.dto.request.BookUpdateRequest;
import com.nikookinn.librarymanagement.service.BookService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/books")
public class BookController implements BookApi {
    private final BookService bookService;

    public BookController(BookService bookService) {
        this.bookService = bookService;
    }

    @Override
    @GetMapping
    public ResponseEntity<Page<BookResponse>> getAllBooks(
            @PageableDefault(size = 10, page = 0) Pageable pageable) {
        Page<BookResponse> books = bookService.getAllBooks(pageable);
        return ResponseEntity.ok(books);
    }

    @Override
    @GetMapping("/{id}")
    public ResponseEntity<BookResponse> getBookById(@PathVariable Long id) {
        BookResponse book = bookService.getBookById(id);
        return ResponseEntity.ok(book);
    }

    @Override
    @PostMapping
    public ResponseEntity<BookResponse> createBook(@Valid @RequestBody BookCreateRequest request) {
        BookResponse created = bookService.createBook(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @Override
    @PutMapping("/{id}")
    public ResponseEntity<BookResponse> updateBook(
            @PathVariable Long id,
            @Valid @RequestBody BookUpdateRequest request) {
        BookResponse updated = bookService.updateBook(id, request);
        return ResponseEntity.ok(updated);
    }

    @Override
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBook(@PathVariable Long id) {
        bookService.deleteBook(id);
        return ResponseEntity.noContent().build();
    }

    @Override
    @GetMapping("/category/{categoryId}")
    public ResponseEntity<Page<BookResponse>> getBooksByCategory(
            @PathVariable Long categoryId,
            @PageableDefault(size = 10, page = 0) Pageable pageable) {
        Page<BookResponse> books = bookService.getBooksByCategory(categoryId, pageable);
        return ResponseEntity.ok(books);
    }

    @Override
    @GetMapping("/author/{authorId}")
    public ResponseEntity<Page<BookResponse>> getBooksByAuthor(
            @PathVariable Long authorId,
            @PageableDefault(size = 10, page = 0) Pageable pageable) {
        Page<BookResponse> books = bookService.getBooksByAuthor(authorId, pageable);
        return ResponseEntity.ok(books);
    }

    @Override
    @GetMapping("/search")
    public ResponseEntity<Page<BookResponse>> searchBooks(
            @RequestParam String query,
            @PageableDefault(size = 10, page = 0) Pageable pageable) {
        Page<BookResponse> books = bookService.searchBooks(query, pageable);
        return ResponseEntity.ok(books);
    }

    @Override
    @GetMapping("/available")
    public ResponseEntity<Page<BookResponse>> getAvailableBooks(
            @PageableDefault(size = 10, page = 0) Pageable pageable) {
        Page<BookResponse> books = bookService.getAvailableBooks(pageable);
        return ResponseEntity.ok(books);
    }

    @Override
    @PostMapping("/{bookId}/authors/{authorId}")
    public ResponseEntity<Void> addAuthorToBook(
            @PathVariable Long bookId,
            @PathVariable Long authorId) {
        bookService.addAuthorToBook(bookId, authorId);
        return ResponseEntity.noContent().build();
    }

    @Override
    @DeleteMapping("/{bookId}/authors/{authorId}")
    public ResponseEntity<Void> removeAuthorFromBook(
            @PathVariable Long bookId,
            @PathVariable Long authorId) {
        bookService.removeAuthorFromBook(bookId, authorId);
        return ResponseEntity.noContent().build();
    }
}
