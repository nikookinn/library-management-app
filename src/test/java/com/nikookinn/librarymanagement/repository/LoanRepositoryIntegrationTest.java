package com.nikookinn.librarymanagement.repository;

import com.nikookinn.librarymanagement.dto.response.OverdueLoanResponse;
import com.nikookinn.librarymanagement.entity.*;
import com.nikookinn.librarymanagement.testsupport.AbstractIntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
@DisplayName("Loan Repository Custom Query Tests")
class LoanRepositoryIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private LoanRepository loanRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    private Member member;
    private Book book;
    private Loan overdueLoan;
    private Loan activeLoan;

    @BeforeEach
    void setUp() {
        loanRepository.deleteAll();
        bookRepository.deleteAll();
        categoryRepository.deleteAll();
        memberRepository.deleteAll();

        member = new Member();
        member.setFirstName("Alice");
        member.setLastName("Wonderland");
        member.setEmail("alice@example.com");
        member.setMembershipDate(LocalDate.now());
        member = memberRepository.save(member);

        Category category = new Category();
        category.setName("Literature");
        category = categoryRepository.save(category);

        book = new Book();
        book.setTitle("Classic Novel");
        book.setIsbn("ISBN-999");
        book.setCategory(category);
        book.setTotalCopies(10);
        book.setAvailableCopies(10);
        book = bookRepository.save(book);

        // This loan is already overdue but still has status ACTIVE
        overdueLoan = new Loan();
        overdueLoan.setMember(member);
        overdueLoan.setBook(book);
        overdueLoan.setBorrowDate(LocalDateTime.now().minusDays(20));
        overdueLoan.setDueDate(LocalDateTime.now().minusDays(5));
        overdueLoan.setStatus(LoanStatus.ACTIVE);
        overdueLoan = loanRepository.save(overdueLoan);

        // This loan is active and not overdue
        activeLoan = new Loan();
        activeLoan.setMember(member);
        activeLoan.setBook(book);
        activeLoan.setBorrowDate(LocalDateTime.now().minusDays(2));
        activeLoan.setDueDate(LocalDateTime.now().plusDays(10));
        activeLoan.setStatus(LoanStatus.ACTIVE);
        activeLoan = loanRepository.save(activeLoan);
    }

    @Test
    @DisplayName("should mark active loans as overdue when due date has passed")
    void shouldMarkOverdueLoans() {
        // Act
        int updatedCount = loanRepository.markOverdueLoans(LocalDateTime.now());

        // Assert
        assertThat(updatedCount).isEqualTo(1);
        
        Loan updatedLoan = loanRepository.findById(overdueLoan.getId()).orElseThrow();
        assertThat(updatedLoan.getStatus()).isEqualTo(LoanStatus.OVERDUE);
        
        Loan stillActiveLoan = loanRepository.findById(activeLoan.getId()).orElseThrow();
        assertThat(stillActiveLoan.getStatus()).isEqualTo(LoanStatus.ACTIVE);
    }

    @Test
    @DisplayName("should count active loans by member")
    void shouldCountActiveLoansByMember() {
        // Act
        long count = loanRepository.countActiveLoansByMember(member.getId());

        // Assert
        // Both overdue with status active and regular active loan count as Active/Overdue for this method
        assertThat(count).isEqualTo(2);
    }

    @Test
    @DisplayName("should find overdue details as DTO projection")
    void shouldFindOverdueDetails() {
        // Arrange: Make sure the status is overdue for the DTO query to pick it up
        overdueLoan.setStatus(LoanStatus.OVERDUE);
        loanRepository.save(overdueLoan);

        // Act
        List<OverdueLoanResponse> results = loanRepository.findOverdueDetails();

        // Assert
        assertThat(results).hasSize(1);
        OverdueLoanResponse dto = results.getFirst();
        assertThat(dto.loanId()).isEqualTo(overdueLoan.getId());
        assertThat(dto.memberName()).isEqualTo("Alice Wonderland");
        assertThat(dto.bookTitle()).isEqualTo("Classic Novel");
    }

    @Test
    @DisplayName("should find active loans by book")
    void shouldFindActiveLoansByBook() {
        // Act
        List<Loan> results = loanRepository.findActiveLoansByBook(book.getId());

        // Assert
        assertThat(results).hasSize(2);
        assertThat(results.getFirst().getId()).isEqualTo(activeLoan.getId());
    }
}
