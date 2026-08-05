package com.nikookinn.librarymanagement.controller.api;

import com.nikookinn.librarymanagement.dto.request.AuthorCreateRequest;
import com.nikookinn.librarymanagement.dto.request.AuthorUpdateRequest;
import com.nikookinn.librarymanagement.dto.response.AuthorResponse;
import com.nikookinn.librarymanagement.dto.response.ProlificAuthorResponse;
import com.nikookinn.librarymanagement.exception.ErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;

import java.util.List;

@Tag(name = "Author", description = "Operations for managing authors")
public interface AuthorApi {

    @Operation(summary = "List all authors")
    ResponseEntity<Page<AuthorResponse>> getAllAuthors(Pageable pageable);

    @Operation(summary = "Get author by ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Author found"),
            @ApiResponse(responseCode = "404", description = "Author not found",
                         content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    ResponseEntity<AuthorResponse> getAuthorById(Long id);

    @Operation(summary = "Create a new author")
    ResponseEntity<AuthorResponse> createAuthor(AuthorCreateRequest request);

    @Operation(summary = "Update an author")
    ResponseEntity<AuthorResponse> updateAuthor(Long id, AuthorUpdateRequest request);

    @Operation(summary = "Delete an author")
    ResponseEntity<Void> deleteAuthor(Long id);

    @Operation(summary = "Search authors by name")
    ResponseEntity<Page<AuthorResponse>> searchAuthors(String name, Pageable pageable);

    @Operation(summary = "Get authors by category name")
    ResponseEntity<List<AuthorResponse>> getAuthorsByCategory(String categoryName);

    @Operation(summary = "Get authors who write in multiple categories")
    ResponseEntity<List<AuthorResponse>> getMultiCategoryAuthors();

    @Operation(summary = "Get authors who have published books")
    ResponseEntity<List<AuthorResponse>> getAuthorsWithBooks();

    @Operation(summary = "Get authors by nationality")
    ResponseEntity<List<AuthorResponse>> getAuthorsByNationality(String nationality);

    @Operation(summary = "Get prolific authors (with minimum book count)")
    ResponseEntity<List<ProlificAuthorResponse>> getProlificAuthors(int minBooks);
}
