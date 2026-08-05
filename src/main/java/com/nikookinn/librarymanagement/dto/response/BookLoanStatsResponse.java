package com.nikookinn.librarymanagement.dto.response;

public record BookLoanStatsResponse(
        Long bookId,
        String title,
        String isbn,
        long loanCount
) {}
