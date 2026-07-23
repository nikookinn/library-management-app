package com.nikookinn.librarymanagement.mapper;

import com.nikookinn.librarymanagement.dto.response.LoanResponse;
import com.nikookinn.librarymanagement.entity.Loan;

public final class LoanMapper {
    private LoanMapper() {
    }

    public static LoanResponse toResponse(Loan loan) {
        return new LoanResponse(loan.getId(), loan.getBorrowDate(), loan.getDueDate(), loan.getReturnDate(),
                loan.getStatus(), loan.getMember() != null ? loan.getMember().getId() : null,
                loan.getBook() != null ? loan.getBook().getId() : null);
    }
}
