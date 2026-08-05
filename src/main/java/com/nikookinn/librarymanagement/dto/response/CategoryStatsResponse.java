package com.nikookinn.librarymanagement.dto.response;

public record CategoryStatsResponse(
        String categoryName,
        long loanCount
) {}
