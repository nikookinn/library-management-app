package com.nikookinn.librarymanagement.service.impl;

import com.nikookinn.librarymanagement.dto.request.LoanCreateRequest;
import com.nikookinn.librarymanagement.entity.*;
import com.nikookinn.librarymanagement.repository.BookRepository;
import com.nikookinn.librarymanagement.repository.LoanRepository;
import com.nikookinn.librarymanagement.repository.MemberRepository;
import com.nikookinn.librarymanagement.repository.CategoryRepository;
import com.nikookinn.librarymanagement.service.LoanService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.CacheManager;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;

@SpringBootTest
@DisplayName("Loan Service Integration Tests")
class LoanServiceIntegrationTest {

    @Autowired
    private LoanService loanService;

    @MockitoSpyBean
    private LoanRepository loanRepository;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private CacheManager cacheManager;

    private Book book;
    private Member member;

    @BeforeEach
    void setUp() {
        cacheManager.getCacheNames().forEach(name -> {
            var cache = cacheManager.getCache(name);
            if (cache != null) cache.clear();
        });

        loanRepository.deleteAll();
        bookRepository.deleteAll();
        memberRepository.deleteAll();
        categoryRepository.deleteAll();

        Category category = new Category();
        category.setName("Fiction");
        category = categoryRepository.save(category);

        book = new Book();
        book.setTitle("Test Book");
        book.setIsbn("1234567890");
        book.setTotalCopies(10);
        book.setAvailableCopies(10);
        book.setCategory(category);
        book = bookRepository.save(book);

        member = new Member();
        member.setFirstName("John");
        member.setLastName("Doe");
        member.setEmail("john.doe@example.com");
        member.setMembershipDate(LocalDate.now());
        member = memberRepository.save(member);
    }

    @Test
    @DisplayName("should rollback book copy decrement when loan save fails in createLoan")
    void shouldRollbackBookDecrementWhenLoanSaveFails() {
        LoanCreateRequest request = new LoanCreateRequest(member.getId(), book.getId(), LocalDateTime.now().plusDays(14));
        
        doThrow(new RuntimeException("Simulated error")).when(loanRepository).save(any(Loan.class));

        assertThatThrownBy(() -> loanService.createLoan(request))
                .isInstanceOf(RuntimeException.class);

        Book updatedBook = bookRepository.findById(book.getId()).orElseThrow();
        assertThat(updatedBook.getAvailableCopies()).isEqualTo(10);
        assertThat(loanRepository.count()).isZero();
    }

    @Test
    @DisplayName("should rollback book copy increment when loan save fails in returnLoan")
    void shouldRollbackBookIncrementWhenLoanSaveFails() {
        Loan loan = new Loan();
        loan.setBook(book);
        loan.setMember(member);
        loan.setBorrowDate(LocalDateTime.now());
        loan.setDueDate(LocalDateTime.now().plusDays(14));
        loan.setStatus(LoanStatus.ACTIVE);
        
        book.setAvailableCopies(9);
        bookRepository.save(book);
        Loan savedLoan = loanRepository.save(loan);

        doThrow(new RuntimeException("Simulated error")).when(loanRepository).save(any(Loan.class));

        assertThatThrownBy(() -> loanService.returnLoan(savedLoan.getId()))
                .isInstanceOf(RuntimeException.class);

        Book updatedBook = bookRepository.findById(book.getId()).orElseThrow();
        assertThat(updatedBook.getAvailableCopies()).isEqualTo(9);
        
        Loan currentLoan = loanRepository.findById(savedLoan.getId()).orElseThrow();
        assertThat(currentLoan.getStatus()).isEqualTo(LoanStatus.ACTIVE);
    }

    @Test
    @DisplayName("should evict book cache when new loan is created")
    void shouldEvictBookCacheOnLoanCreate() {
        // Fill cache
        cacheManager.getCache("books").put(book.getId(), "Cached Book Data");
        assertThat(cacheManager.getCache("books").get(book.getId())).isNotNull();

        LoanCreateRequest request = new LoanCreateRequest(member.getId(), book.getId(), LocalDateTime.now().plusDays(14));
        loanService.createLoan(request);

        // Verify book cache is cleared because availableCopies changed
        assertThat(cacheManager.getCache("books").get(book.getId())).isNull();
    }

    @Test
    @DisplayName("should evict book cache when loan is returned")
    void shouldEvictBookCacheOnLoanReturn() {
        // Prepare book state: 1 copy is loaned out
        book.setAvailableCopies(9);
        bookRepository.save(book);

        Loan loan = new Loan();
        loan.setBook(book);
        loan.setMember(member);
        loan.setBorrowDate(LocalDateTime.now());
        loan.setDueDate(LocalDateTime.now().plusDays(14));
        loan.setStatus(LoanStatus.ACTIVE);
        Loan savedLoan = loanRepository.save(loan);

        // Fill cache
        cacheManager.getCache("books").put(book.getId(), "Cached Book Data");
        assertThat(cacheManager.getCache("books").get(book.getId())).isNotNull();

        loanService.returnLoan(savedLoan.getId());

        // Verify book cache is cleared
        assertThat(cacheManager.getCache("books").get(book.getId())).isNull();
    }
}
