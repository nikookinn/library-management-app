package com.nikookinn.librarymanagement.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record BookUpdateRequest(
        @NotBlank(message = "Title cannot be blank")
        String title,

        @NotBlank(message = "ISBN cannot be blank")
        String isbn,

        Integer publishYear,

        String description,

        @NotNull(message = "Total copies cannot be null")
        @Positive(message = "Total copies must be positive")
        Integer totalCopies,

        @NotNull(message = "Category ID cannot be null")
        Long categoryId) {
}
