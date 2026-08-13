package com.nikookinn.librarymanagement.dto.response;

public record BookResponse(Long id, String title, String isbn, Integer publishYear, String description,
                           Integer totalCopies, Integer availableCopies, Long categoryId, String coverImage) {
}
