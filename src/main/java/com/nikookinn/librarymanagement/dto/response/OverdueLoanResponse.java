package com.nikookinn.librarymanagement.dto.response;

import java.time.LocalDateTime;

public record OverdueLoanResponse(
        Long loanId,
        String memberName,
        String bookTitle,
        LocalDateTime dueDate,
        long daysOverdue
) {}
