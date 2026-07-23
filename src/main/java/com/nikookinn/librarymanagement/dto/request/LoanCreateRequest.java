package com.nikookinn.librarymanagement.dto.request;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record LoanCreateRequest(
        @NotNull(message = "Member ID cannot be null")
        Long memberId,

        @NotNull(message = "Book ID cannot be null")
        Long bookId,

        @NotNull(message = "Due date cannot be null")
        @Future(message = "Due date must be in the future")
        LocalDateTime dueDate) {
}
