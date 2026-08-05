package com.nikookinn.librarymanagement.controller.api;

import com.nikookinn.librarymanagement.dto.request.LoanCreateRequest;
import com.nikookinn.librarymanagement.dto.request.LoanUpdateRequest;
import com.nikookinn.librarymanagement.dto.response.LoanResponse;
import com.nikookinn.librarymanagement.dto.response.OverdueLoanResponse;
import com.nikookinn.librarymanagement.entity.LoanStatus;
import com.nikookinn.librarymanagement.exception.ErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;

import java.util.List;

@Tag(name = "Loan", description = "Operations for managing book loans")
public interface LoanApi {

    @Operation(summary = "List all loan records")
    ResponseEntity<Page<LoanResponse>> getAllLoans(Pageable pageable);

    @Operation(summary = "Get loan record by ID")
    ResponseEntity<LoanResponse> getLoanById(Long id);

    @Operation(summary = "Create a new loan record")
    ResponseEntity<LoanResponse> createLoan(LoanCreateRequest request);

    @Operation(summary = "Update a loan record")
    ResponseEntity<LoanResponse> updateLoan(Long id, LoanUpdateRequest request);

    @Operation(summary = "Delete a loan record")
    ResponseEntity<Void> deleteLoan(Long id);

    @Operation(summary = "Get loan records by member")
    ResponseEntity<Page<LoanResponse>> getLoansByMember(Long memberId, Pageable pageable);

    @Operation(summary = "Get loan records by status")
    ResponseEntity<Page<LoanResponse>> getLoansByStatus(LoanStatus status, Pageable pageable);

    @Operation(summary = "Get active loan records")
    ResponseEntity<Page<LoanResponse>> getActiveLoans(Pageable pageable);

    @Operation(summary = "Get overdue loan records")
    ResponseEntity<Page<LoanResponse>> getOverdueLoans(Pageable pageable);

    @Operation(summary = "Return a borrowed book")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Book returned successfully"),
            @ApiResponse(responseCode = "404", description = "Loan record not found",
                         content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    ResponseEntity<LoanResponse> returnLoan(Long id);

    @Operation(summary = "Get detailed overdue report")
    ResponseEntity<List<OverdueLoanResponse>> getOverdueDetails();

    @Operation(summary = "Get loans by member and status")
    ResponseEntity<Page<LoanResponse>> getLoansByMemberAndStatus(Long memberId, LoanStatus status, Pageable pageable);

    @Operation(summary = "Get loans by member with full details")
    ResponseEntity<List<LoanResponse>> getLoansByMemberWithDetails(Long memberId, LoanStatus status);

    @Operation(summary = "Get active loans for a specific book")
    ResponseEntity<List<LoanResponse>> getActiveLoansByBook(Long bookId);
}
