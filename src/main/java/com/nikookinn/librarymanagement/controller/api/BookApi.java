package com.nikookinn.librarymanagement.controller.api;

import com.nikookinn.librarymanagement.dto.request.BookCreateRequest;
import com.nikookinn.librarymanagement.dto.request.BookSearchRequest;
import com.nikookinn.librarymanagement.dto.request.BookUpdateRequest;
import com.nikookinn.librarymanagement.dto.response.BookResponse;
import com.nikookinn.librarymanagement.dto.response.CategoryStatsResponse;
import com.nikookinn.librarymanagement.dto.response.BookLoanStatsResponse;
import com.nikookinn.librarymanagement.exception.ErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;

import java.util.List;

@Tag(name = "Book", description = "Operations for managing books")
public interface BookApi {

    @Operation(summary = "List all books", description = "Retrieves all books in a paginated format")
    ResponseEntity<Page<BookResponse>> getAllBooks(Pageable pageable);

    @Operation(summary = "Get book by ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Book found"),
            @ApiResponse(responseCode = "404", description = "Book not found", 
                         content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    ResponseEntity<BookResponse> getBookById(@Parameter(description = "Book ID") Long id);

    @Operation(summary = "Create a new book")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Book created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input",
                         content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    ResponseEntity<BookResponse> createBook(BookCreateRequest request);

    @Operation(summary = "Update a book")
    ResponseEntity<BookResponse> updateBook(Long id, BookUpdateRequest request);

    @Operation(summary = "Delete a book")
    ResponseEntity<Void> deleteBook(Long id);

    @Operation(summary = "Get books by category")
    ResponseEntity<Page<BookResponse>> getBooksByCategory(Long categoryId, Pageable pageable);

    @Operation(summary = "Get books by author")
    ResponseEntity<Page<BookResponse>> getBooksByAuthor(Long authorId, Pageable pageable);

    @Operation(summary = "Search for books")
    ResponseEntity<Page<BookResponse>> searchBooks(String query, Pageable pageable);

    @Operation(summary = "Dynamic search for books with multiple filters")
    ResponseEntity<Page<BookResponse>> searchBooksDynamic(BookSearchRequest request, Pageable pageable);

    @Operation(summary = "List available books for loan")
    ResponseEntity<Page<BookResponse>> getAvailableBooks(Pageable pageable);

    @Operation(summary = "Add author to a book")
    ResponseEntity<Void> addAuthorToBook(Long bookId, Long authorId);

    @Operation(summary = "Remove author from a book")
    ResponseEntity<Void> removeAuthorFromBook(Long bookId, Long authorId);

    @Operation(summary = "Get books that were never borrowed")
    ResponseEntity<List<BookResponse>> getBooksNeverBorrowed();

    @Operation(summary = "Get top categories by loan count")
    ResponseEntity<List<CategoryStatsResponse>> getTopCategories();

    @Operation(summary = "Get available books with full details")
    ResponseEntity<List<BookResponse>> getAvailableBooksWithDetails(Pageable pageable);

    @Operation(summary = "Get books by category and minimum availability")
    ResponseEntity<List<BookResponse>> getBooksByCategoryAndAvailability(Long categoryId, int minCopies);

    @Operation(summary = "Get most borrowed books")
    ResponseEntity<List<BookLoanStatsResponse>> getMostBorrowedBooks(int limit);
}
