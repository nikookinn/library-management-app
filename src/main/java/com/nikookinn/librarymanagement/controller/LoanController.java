package com.nikookinn.librarymanagement.controller;

import com.nikookinn.librarymanagement.controller.api.LoanApi;
import com.nikookinn.librarymanagement.dto.request.LoanCreateRequest;
import com.nikookinn.librarymanagement.dto.response.LoanResponse;
import com.nikookinn.librarymanagement.dto.request.LoanUpdateRequest;
import com.nikookinn.librarymanagement.entity.LoanStatus;
import com.nikookinn.librarymanagement.service.LoanService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/loans")
public class LoanController implements LoanApi {
    private final LoanService loanService;

    public LoanController(LoanService loanService) {
        this.loanService = loanService;
    }

    @Override
    @GetMapping
    public ResponseEntity<Page<LoanResponse>> getAllLoans(
            @PageableDefault(size = 10, page = 0) Pageable pageable) {
        Page<LoanResponse> loans = loanService.getAllLoans(pageable);
        return ResponseEntity.ok(loans);
    }

    @Override
    @GetMapping("/{id}")
    public ResponseEntity<LoanResponse> getLoanById(@PathVariable Long id) {
        LoanResponse loan = loanService.getLoanById(id);
        return ResponseEntity.ok(loan);
    }

    @Override
    @PostMapping
    public ResponseEntity<LoanResponse> createLoan(@Valid @RequestBody LoanCreateRequest request) {
        LoanResponse created = loanService.createLoan(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @Override
    @PutMapping("/{id}")
    public ResponseEntity<LoanResponse> updateLoan(
            @PathVariable Long id,
            @Valid @RequestBody LoanUpdateRequest request) {
        LoanResponse updated = loanService.updateLoan(id, request);
        return ResponseEntity.ok(updated);
    }

    @Override
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteLoan(@PathVariable Long id) {
        loanService.deleteLoan(id);
        return ResponseEntity.noContent().build();
    }

    @Override
    @GetMapping("/member/{memberId}")
    public ResponseEntity<Page<LoanResponse>> getLoansByMember(
            @PathVariable Long memberId,
            @PageableDefault(size = 10, page = 0) Pageable pageable) {
        Page<LoanResponse> loans = loanService.getLoansByMember(memberId, pageable);
        return ResponseEntity.ok(loans);
    }

    @Override
    @GetMapping("/status/{status}")
    public ResponseEntity<Page<LoanResponse>> getLoansByStatus(
            @PathVariable LoanStatus status,
            @PageableDefault(size = 10, page = 0) Pageable pageable) {
        Page<LoanResponse> loans = loanService.getLoansByStatus(status, pageable);
        return ResponseEntity.ok(loans);
    }

    @Override
    @GetMapping("/active")
    public ResponseEntity<Page<LoanResponse>> getActiveLoans(
            @PageableDefault(size = 10, page = 0) Pageable pageable) {
        Page<LoanResponse> loans = loanService.getActiveLoan(pageable);
        return ResponseEntity.ok(loans);
    }

    @Override
    @GetMapping("/overdue")
    public ResponseEntity<Page<LoanResponse>> getOverdueLoans(
            @PageableDefault(size = 10, page = 0) Pageable pageable) {
        Page<LoanResponse> loans = loanService.getOverdueLoans(pageable);
        return ResponseEntity.ok(loans);
    }

    @Override
    @PutMapping("/{id}/return")
    public ResponseEntity<LoanResponse> returnLoan(@PathVariable Long id) {
        LoanResponse returned = loanService.returnLoan(id);
        return ResponseEntity.ok(returned);
    }
}
