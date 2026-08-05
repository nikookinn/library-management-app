package com.nikookinn.librarymanagement.repository;

import com.nikookinn.librarymanagement.entity.Book;
import com.nikookinn.librarymanagement.entity.Loan;
import com.nikookinn.librarymanagement.entity.LoanStatus;
import com.nikookinn.librarymanagement.entity.Member;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
@DisplayName("Loan Repository Integration Tests")
class LoanRepositoryTest {

    @Autowired
    private LoanRepository loanRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private BookRepository bookRepository;

    private Member member;
    private Book book;
    private Loan activeLoan;
    private Loan overdueLoan;

    @BeforeEach
    void setUp() {
        member = new Member();
        member.setFirstName("Frodo");
        member.setLastName("Baggins");
        member.setEmail("frodo@shire.com");
        member = memberRepository.save(member);

        book = new Book();
        book.setTitle("The Fellowship of the Ring");
        book.setIsbn("978-0618640157");
        book.setTotalCopies(1);
        book.setAvailableCopies(1);
        book = bookRepository.save(book);

        activeLoan = new Loan();
        activeLoan.setMember(member);
        activeLoan.setBook(book);
        activeLoan.setBorrowDate(LocalDateTime.now());
        activeLoan.setDueDate(LocalDateTime.now().plusDays(14));
        activeLoan.setStatus(LoanStatus.ACTIVE);
        loanRepository.save(activeLoan);

        overdueLoan = new Loan();
        overdueLoan.setMember(member);
        overdueLoan.setBook(book);
        overdueLoan.setBorrowDate(LocalDateTime.now().minusDays(20));
        overdueLoan.setDueDate(LocalDateTime.now().minusDays(6));
        overdueLoan.setStatus(LoanStatus.ACTIVE);
        loanRepository.save(overdueLoan);
    }

    @Test
    @DisplayName("should find loans by member ID")
    void shouldFindLoansByMemberId() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);

        // Act
        Page<Loan> result = loanRepository.findByMember_Id(member.getId(), pageable);

        // Assert
        assertThat(result.getContent()).hasSize(2);
    }

    @Test
    @DisplayName("should find loans by status")
    void shouldFindLoansByStatus() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);

        // Act
        Page<Loan> result = loanRepository.findByStatus(LoanStatus.ACTIVE, pageable);

        // Assert
        assertThat(result.getContent()).hasSize(2);
    }

    @Test
    @DisplayName("should count active loans for book")
    void shouldCountActiveLoansForBook() {
        // Act
        long count = loanRepository.countByBook_IdAndStatusIn(book.getId(), List.of(LoanStatus.ACTIVE));

        // Assert
        assertThat(count).isEqualTo(2);
    }

    @Test
    @DisplayName("should mark overdue loans")
    void shouldMarkOverdueLoans() {
        // Act
        int updatedCount = loanRepository.markOverdueLoans(LocalDateTime.now());
        
        // Assert
        assertThat(updatedCount).isEqualTo(1);
        
        Loan updatedLoan = loanRepository.findById(overdueLoan.getId()).orElseThrow();
        assertThat(updatedLoan.getStatus()).isEqualTo(LoanStatus.OVERDUE);
    }
}

