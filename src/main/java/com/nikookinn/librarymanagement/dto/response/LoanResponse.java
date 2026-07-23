package com.nikookinn.librarymanagement.dto.response;

import com.nikookinn.librarymanagement.entity.LoanStatus;

import java.time.LocalDateTime;

public record LoanResponse(Long id, LocalDateTime borrowDate, LocalDateTime dueDate, LocalDateTime returnDate,
                           LoanStatus status, Long memberId, Long bookId) {
}
