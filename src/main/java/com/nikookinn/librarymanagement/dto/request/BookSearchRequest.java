package com.nikookinn.librarymanagement.dto.request;

public record BookSearchRequest(
        String title,
        String authorName,
        String categoryName,
        String isbn,
        Integer minAvailableCopies,
        Integer publishYear
) {}
