package com.nikookinn.librarymanagement.dto.response;

public record ProlificAuthorResponse(
        Long authorId,
        String fullName,
        long bookCount
) {}
