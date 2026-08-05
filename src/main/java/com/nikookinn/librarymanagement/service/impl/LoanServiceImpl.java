package com.nikookinn.librarymanagement.service.impl;

import com.nikookinn.librarymanagement.dto.request.LoanCreateRequest;
import com.nikookinn.librarymanagement.dto.request.LoanUpdateRequest;
import com.nikookinn.librarymanagement.dto.response.LoanResponse;
import com.nikookinn.librarymanagement.dto.response.OverdueLoanResponse;
import com.nikookinn.librarymanagement.entity.Book;
import com.nikookinn.librarymanagement.entity.Loan;
import com.nikookinn.librarymanagement.entity.LoanStatus;
import com.nikookinn.librarymanagement.entity.Member;
import com.nikookinn.librarymanagement.exception.BusinessRuleViolationException;
import com.nikookinn.librarymanagement.exception.ResourceNotFoundException;
import com.nikookinn.librarymanagement.mapper.LoanMapper;
import com.nikookinn.librarymanagement.repository.BookRepository;
import com.nikookinn.librarymanagement.repository.LoanRepository;
import com.nikookinn.librarymanagement.repository.MemberRepository;
import com.nikookinn.librarymanagement.service.LoanService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class LoanServiceImpl implements LoanService {
    private final LoanRepository loanRepository;
    private final BookRepository bookRepository;
    private final MemberRepository memberRepository;

    public LoanServiceImpl(LoanRepository loanRepository, BookRepository bookRepository, MemberRepository memberRepository) {
        this.loanRepository = loanRepository;
        this.bookRepository = bookRepository;
        this.memberRepository = memberRepository;
    }

    @Override
    @Transactional
    public Page<LoanResponse> getAllLoans(Pageable pageable) {
        markOverdueLoans();
        return loanRepository.findAll(pageable).map(LoanMapper::toResponse);
    }

    @Override
    @Transactional
    public LoanResponse getLoanById(Long id) {
        markOverdueLoans();
        return loanRepository.findById(id)
                .map(LoanMapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Loan not found with id: " + id));
    }

    @Override
    @Transactional
    public LoanResponse createLoan(LoanCreateRequest request) {
        Book book = bookRepository.findById(request.bookId())
                .orElseThrow(() -> new ResourceNotFoundException("Book not found with id: " + request.bookId()));
        Member member = memberRepository.findById(request.memberId())
                .orElseThrow(() -> new ResourceNotFoundException("Member not found with id: " + request.memberId()));

        if (book.getAvailableCopies() == null || book.getAvailableCopies() <= 0) {
            throw new BusinessRuleViolationException("Book is not available for loan: " + book.getId());
        }

        Loan loan = new Loan();
        loan.setBook(book);
        loan.setMember(member);
        loan.setBorrowDate(LocalDateTime.now());
        loan.setDueDate(request.dueDate());
        loan.setStatus(LoanStatus.ACTIVE);

        book.setAvailableCopies(book.getAvailableCopies() - 1);
        bookRepository.save(book);
        return LoanMapper.toResponse(loanRepository.save(loan));
    }

    @Override
    @Transactional
    public LoanResponse updateLoan(Long id, LoanUpdateRequest request) {
        markOverdueLoans();
        Loan loan = loanRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Loan not found with id: " + id));

        if (loan.getStatus() == LoanStatus.RETURNED) {
            throw new BusinessRuleViolationException("A returned loan cannot be updated");
        }

        loan.setDueDate(request.dueDate());
        loan.setStatus(LoanStatus.ACTIVE);
        return LoanMapper.toResponse(loanRepository.save(loan));
    }

    @Override
    @Transactional
    public void deleteLoan(Long id) {
        Loan loan = loanRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Loan not found with id: " + id));
        if (loan.getStatus() != LoanStatus.RETURNED) {
            throw new BusinessRuleViolationException("An active loan cannot be deleted; return the book first");
        }
        loanRepository.deleteById(id);
    }

    @Override
    @Transactional
    public Page<LoanResponse> getLoansByMember(Long memberId, Pageable pageable) {
        markOverdueLoans();
        memberRepository.findById(memberId)
                .orElseThrow(() -> new ResourceNotFoundException("Member not found with id: " + memberId));
        return loanRepository.findByMember_Id(memberId, pageable).map(LoanMapper::toResponse);
    }

    @Override
    @Transactional
    public Page<LoanResponse> getLoansByStatus(LoanStatus status, Pageable pageable) {
        markOverdueLoans();
        return loanRepository.findByStatus(status, pageable).map(LoanMapper::toResponse);
    }

    @Override
    @Transactional
    public Page<LoanResponse> getActiveLoan(Pageable pageable) {
        markOverdueLoans();
        return loanRepository.findByStatus(LoanStatus.ACTIVE, pageable).map(LoanMapper::toResponse);
    }

    @Override
    @Transactional
    public Page<LoanResponse> getOverdueLoans(Pageable pageable) {
        markOverdueLoans();
        return loanRepository.findByStatus(LoanStatus.OVERDUE, pageable).map(LoanMapper::toResponse);
    }

    @Override
    @Transactional
    public LoanResponse returnLoan(Long loanId) {
        markOverdueLoans();
        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new ResourceNotFoundException("Loan not found with id: " + loanId));
        if (loan.getStatus() == LoanStatus.RETURNED) {
            throw new BusinessRuleViolationException("Loan has already been returned");
        }

        Book book = loan.getBook();
        if (book.getAvailableCopies() == null || book.getTotalCopies() == null
                || book.getAvailableCopies() >= book.getTotalCopies()) {
            throw new BusinessRuleViolationException("Book inventory is inconsistent for book id: " + book.getId());
        }

        loan.setStatus(LoanStatus.RETURNED);
        loan.setReturnDate(LocalDateTime.now());
        book.setAvailableCopies(book.getAvailableCopies() + 1);
        bookRepository.save(book);
        return LoanMapper.toResponse(loanRepository.save(loan));
    }

    @Override
    @Transactional(readOnly = true)
    public List<OverdueLoanResponse> getOverdueDetails() {
        markOverdueLoans();
        LocalDateTime now = LocalDateTime.now();
        return loanRepository.findOverdueDetails().stream()
                .map(d -> new OverdueLoanResponse(
                        d.loanId(),
                        d.memberName(),
                        d.bookTitle(),
                        d.dueDate(),
                        ChronoUnit.DAYS.between(d.dueDate(), now)
                ))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<LoanResponse> getLoansByMemberAndStatus(Long memberId, LoanStatus status, Pageable pageable) {
        return loanRepository.findByMember_IdAndStatus(memberId, status, pageable)
                .map(LoanMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public List<LoanResponse> getLoansByMemberWithDetails(Long memberId, LoanStatus status) {
        return loanRepository.findLoansByMemberWithDetails(memberId, status)
                .stream()
                .map(LoanMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<LoanResponse> getActiveLoansByBook(Long bookId) {
        return loanRepository.findActiveLoansByBook(bookId)
                .stream()
                .map(LoanMapper::toResponse)
                .collect(Collectors.toList());
    }

    private void markOverdueLoans() {
        loanRepository.markOverdueLoans(LocalDateTime.now());
    }
}
