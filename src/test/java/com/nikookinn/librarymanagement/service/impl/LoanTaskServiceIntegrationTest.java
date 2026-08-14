package com.nikookinn.librarymanagement.service.impl;

import com.nikookinn.librarymanagement.entity.Book;
import com.nikookinn.librarymanagement.entity.Category;
import com.nikookinn.librarymanagement.entity.Loan;
import com.nikookinn.librarymanagement.entity.LoanStatus;
import com.nikookinn.librarymanagement.entity.Member;
import com.nikookinn.librarymanagement.repository.BookRepository;
import com.nikookinn.librarymanagement.repository.CategoryRepository;
import com.nikookinn.librarymanagement.repository.LoanRepository;
import com.nikookinn.librarymanagement.repository.MemberRepository;
import com.nikookinn.librarymanagement.service.EmailService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

@SpringBootTest
@Transactional
@DisplayName("Loan Task Service Integration Tests")
class LoanTaskServiceIntegrationTest {

    @Autowired
    private LoanTaskServiceImpl loanTaskService;

    @Autowired
    private LoanRepository loanRepository;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @MockitoBean
    private EmailService emailService;

    private Loan activeLoan;
    private Loan overdueLoanCandidate;

    @BeforeEach
    void setUp() {
        Category category = new Category();
        category.setName("Test Category");
        category = categoryRepository.save(category);

        Book book = new Book();
        book.setTitle("Test Book");
        book.setIsbn("1234567890");
        book.setTotalCopies(10);
        book.setAvailableCopies(10);
        book.setCategory(category);
        book = bookRepository.save(book);

        Member member = new Member();
        member.setFirstName("John");
        member.setLastName("Doe");
        member.setEmail("john.doe@example.com");
        member.setPhone("123456789");
        member = memberRepository.save(member);

        // Future due date - should stay ACTIVE
        activeLoan = new Loan();
        activeLoan.setBook(book);
        activeLoan.setMember(member);
        activeLoan.setBorrowDate(LocalDateTime.now());
        activeLoan.setDueDate(LocalDateTime.now().plusDays(7));
        activeLoan.setStatus(LoanStatus.ACTIVE);
        activeLoan = loanRepository.save(activeLoan);

        // Past due date - should become OVERDUE
        overdueLoanCandidate = new Loan();
        overdueLoanCandidate.setBook(book);
        overdueLoanCandidate.setMember(member);
        overdueLoanCandidate.setBorrowDate(LocalDateTime.now().minusDays(10));
        overdueLoanCandidate.setDueDate(LocalDateTime.now().minusDays(3));
        overdueLoanCandidate.setStatus(LoanStatus.ACTIVE);
        overdueLoanCandidate = loanRepository.save(overdueLoanCandidate);
    }

    @Test
    @DisplayName("should mark overdue loans correctly and send email")
    void shouldMarkOverdueLoans() {
        // Act
        loanTaskService.processOverdueLoans();

        // Assert
        Loan updatedActive = loanRepository.findById(activeLoan.getId()).orElseThrow();
        Loan updatedOverdue = loanRepository.findById(overdueLoanCandidate.getId()).orElseThrow();

        assertThat(updatedActive.getStatus()).isEqualTo(LoanStatus.ACTIVE);
        assertThat(updatedOverdue.getStatus()).isEqualTo(LoanStatus.OVERDUE);

        // Verify email reminder was sent for the overdue loan
        verify(emailService).sendOverdueReminder(
                eq(overdueLoanCandidate.getMember().getEmail()),
                anyString(),
                eq(overdueLoanCandidate.getBook().getTitle()),
                anyString()
        );
    }
}
