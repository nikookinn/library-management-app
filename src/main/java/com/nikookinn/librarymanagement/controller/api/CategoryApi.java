package com.nikookinn.librarymanagement.controller.api;

import com.nikookinn.librarymanagement.dto.request.CategoryCreateRequest;
import com.nikookinn.librarymanagement.dto.request.CategoryUpdateRequest;
import com.nikookinn.librarymanagement.dto.response.CategoryResponse;
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

@Tag(name = "Category", description = "Operations for managing book categories")
public interface CategoryApi {

    @Operation(summary = "List all categories")
    ResponseEntity<Page<CategoryResponse>> getAllCategories(Pageable pageable);

    @Operation(summary = "Get category by ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Category found"),
            @ApiResponse(responseCode = "404", description = "Category not found",
                         content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    ResponseEntity<CategoryResponse> getCategoryById(Long id);

    @Operation(summary = "Create a new category")
    ResponseEntity<CategoryResponse> createCategory(CategoryCreateRequest request);

    @Operation(summary = "Update a category")
    ResponseEntity<CategoryResponse> updateCategory(Long id, CategoryUpdateRequest request);

    @Operation(summary = "Delete a category")
    ResponseEntity<Void> deleteCategory(Long id);
}
