package com.nikookinn.librarymanagement.service.impl;

import com.nikookinn.librarymanagement.dto.request.LoanCreateRequest;
import com.nikookinn.librarymanagement.entity.Book;
import com.nikookinn.librarymanagement.entity.Category;
import com.nikookinn.librarymanagement.entity.Member;
import com.nikookinn.librarymanagement.repository.BookRepository;
import com.nikookinn.librarymanagement.repository.CategoryRepository;
import com.nikookinn.librarymanagement.repository.LoanRepository;
import com.nikookinn.librarymanagement.repository.MemberRepository;
import com.nikookinn.librarymanagement.service.LoanService;
import com.nikookinn.librarymanagement.testsupport.AbstractIntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;

@SpringBootTest
@DisplayName("Async Email Integration Test")
class AsyncEmailIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private LoanService loanService;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private LoanRepository loanRepository;

    @MockitoBean
    private JavaMailSender mailSender;

    private Book book;
    private Member member;

    @BeforeEach
    void setUp() {
        loanRepository.deleteAll();
        bookRepository.deleteAll();
        memberRepository.deleteAll();
        categoryRepository.deleteAll();

        Category category = new Category();
        category.setName("Async Test Category");
        category = categoryRepository.save(category);

        book = new Book();
        book.setTitle("Async Book");
        book.setIsbn("0000000000");
        book.setTotalCopies(10);
        book.setAvailableCopies(10);
        book.setCategory(category);
        book = bookRepository.save(book);

        member = new Member();
        member.setFirstName("Async");
        member.setLastName("User");
        member.setEmail("async@example.com");
        member = memberRepository.save(member);
    }

    @Test
    @DisplayName("should return from createLoan immediately while email sends in background")
    void shouldReturnImmediatelyFromCreateLoan() {
        // Arrange
        LoanCreateRequest request = new LoanCreateRequest(member.getId(), book.getId(), LocalDateTime.now().plusDays(7));
        
        // Simulate a slow mail server (3 seconds)
        doAnswer(invocation -> {
            Thread.sleep(3000);
            return null;
        }).when(mailSender).send(any(org.springframework.mail.SimpleMailMessage.class));

        // Act
        long startTime = System.currentTimeMillis();
        loanService.createLoan(request);
        long endTime = System.currentTimeMillis();

        // Assert
        long duration = endTime - startTime;
        
        // If it was synchronous, duration would be > 3000ms.
        // Asynchronous should be much faster (usually < 1000ms).
        assertThat(duration).isLessThan(1000); 
        
        System.out.println("TEST INFO: Loan created in " + duration + "ms. Email is sending in background...");
    }

    private void logAsyncInfo(String message) {
        System.out.println("TEST INFO: " + message);
    }
}
