package com.nikookinn.librarymanagement.controller;

import com.nikookinn.librarymanagement.controller.api.BookApi;
import com.nikookinn.librarymanagement.dto.request.BookCreateRequest;
import com.nikookinn.librarymanagement.dto.request.BookSearchRequest;
import com.nikookinn.librarymanagement.dto.response.BookResponse;
import com.nikookinn.librarymanagement.dto.request.BookUpdateRequest;
import com.nikookinn.librarymanagement.dto.response.CategoryStatsResponse;
import com.nikookinn.librarymanagement.dto.response.BookLoanStatsResponse;
import com.nikookinn.librarymanagement.service.BookService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

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
    @GetMapping("/search/dynamic")
    public ResponseEntity<Page<BookResponse>> searchBooksDynamic(
            BookSearchRequest request,
            @PageableDefault(size = 10, page = 0) Pageable pageable) {
        Page<BookResponse> books = bookService.searchBooksDynamic(request, pageable);
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

    @Override
    @GetMapping("/never-borrowed")
    public ResponseEntity<List<BookResponse>> getBooksNeverBorrowed() {
        return ResponseEntity.ok(bookService.getBooksNeverBorrowed());
    }

    @Override
    @GetMapping("/top-categories")
    public ResponseEntity<List<CategoryStatsResponse>> getTopCategories() {
        return ResponseEntity.ok(bookService.getTopCategories());
    }

    @Override
    @GetMapping("/available/details")
    public ResponseEntity<List<BookResponse>> getAvailableBooksWithDetails(
            @PageableDefault(size = 10, page = 0) Pageable pageable) {
        return ResponseEntity.ok(bookService.getAvailableBooksWithDetails(pageable));
    }

    @Override
    @GetMapping("/category/{categoryId}/min-copies/{minCopies}")
    public ResponseEntity<List<BookResponse>> getBooksByCategoryAndAvailability(
            @PathVariable Long categoryId,
            @PathVariable int minCopies) {
        return ResponseEntity.ok(bookService.getBooksByCategoryAndAvailability(categoryId, minCopies));
    }

    @Override
    @GetMapping("/most-borrowed")
    public ResponseEntity<List<BookLoanStatsResponse>> getMostBorrowedBooks(
            @RequestParam(defaultValue = "5") int limit) {
        return ResponseEntity.ok(bookService.getMostBorrowedBooks(limit));
    }

    @Override
    @PostMapping(value = "/{id}/cover", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<BookResponse> uploadCoverImage(
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file) {
        BookResponse updated = bookService.uploadCoverImage(id, file);
        return ResponseEntity.ok(updated);
    }

    @Override
    @GetMapping(value = "/{id}/cover", produces = {MediaType.IMAGE_JPEG_VALUE, MediaType.IMAGE_PNG_VALUE})
    public ResponseEntity<byte[]> downloadCoverImage(@PathVariable Long id) {
        byte[] image = bookService.getCoverImage(id);
        return ResponseEntity.ok(image);
    }

    @Override
    @DeleteMapping("/{id}/cover")
    public ResponseEntity<Void> deleteCoverImage(@PathVariable Long id) {
        bookService.deleteCoverImage(id);
        return ResponseEntity.noContent().build();
    }
}
