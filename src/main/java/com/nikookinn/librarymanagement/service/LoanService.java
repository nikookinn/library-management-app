package com.nikookinn.librarymanagement.service;

import com.nikookinn.librarymanagement.dto.request.LoanCreateRequest;
import com.nikookinn.librarymanagement.dto.response.LoanResponse;
import com.nikookinn.librarymanagement.dto.request.LoanUpdateRequest;
import com.nikookinn.librarymanagement.dto.response.OverdueLoanResponse;
import com.nikookinn.librarymanagement.entity.LoanStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface LoanService {
    Page<LoanResponse> getAllLoans(Pageable pageable);
    LoanResponse getLoanById(Long id);
    LoanResponse createLoan(LoanCreateRequest request);
    LoanResponse updateLoan(Long id, LoanUpdateRequest request);
    void deleteLoan(Long id);
    
    Page<LoanResponse> getLoansByMember(Long memberId, Pageable pageable);
    Page<LoanResponse> getLoansByStatus(LoanStatus status, Pageable pageable);
    Page<LoanResponse> getActiveLoan(Pageable pageable);
    Page<LoanResponse> getOverdueLoans(Pageable pageable);
    
    LoanResponse returnLoan(Long loanId);
    List<OverdueLoanResponse> getOverdueDetails();
    Page<LoanResponse> getLoansByMemberAndStatus(Long memberId, LoanStatus status, Pageable pageable);
    List<LoanResponse> getLoansByMemberWithDetails(Long memberId, LoanStatus status);
    List<LoanResponse> getActiveLoansByBook(Long bookId);
}
