package com.nikookinn.librarymanagement.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nikookinn.librarymanagement.dto.request.LoanCreateRequest;
import com.nikookinn.librarymanagement.dto.request.LoanUpdateRequest;
import com.nikookinn.librarymanagement.entity.*;
import com.nikookinn.librarymanagement.repository.BookRepository;
import com.nikookinn.librarymanagement.repository.LoanRepository;
import com.nikookinn.librarymanagement.repository.MemberRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@Transactional
class LoanControllerIntegrationTest {

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private LoanRepository loanRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private BookRepository bookRepository;

    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());

    private Member member;
    private Book book;
    private Loan loan;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        member = new Member();
        member.setFirstName("Frodo");
        member.setLastName("Baggins");
        member.setEmail("frodo@shire.com");
        member.setMembershipDate(LocalDate.now());
        member = memberRepository.save(member);

        book = new Book();
        book.setTitle("The Fellowship of the Ring");
        book.setIsbn("978-0618640157");
        book.setTotalCopies(10);
        book.setAvailableCopies(9); // Decrement available copies as we create a loan below
        book = bookRepository.save(book);

        loan = new Loan();
        loan.setMember(member);
        loan.setBook(book);
        loan.setBorrowDate(LocalDateTime.now());
        loan.setDueDate(LocalDateTime.now().plusDays(14));
        loan.setStatus(LoanStatus.ACTIVE);
        loan = loanRepository.save(loan);
    }

    @Test
    void shouldGetAllLoans() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/loans"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)));
    }

    @Test
    void shouldGetLoanById() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/loans/{id}", loan.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ACTIVE"));
    }

    @Test
    void shouldCreateLoan() throws Exception {
        // Arrange
        LoanCreateRequest request = new LoanCreateRequest(
                member.getId(),
                book.getId(),
                LocalDateTime.now().plusDays(20)
        );

        // Act & Assert
        mockMvc.perform(post("/api/loans")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("ACTIVE"));
    }

    @Test
    void shouldReturnLoan() throws Exception {
        // Act & Assert
        mockMvc.perform(put("/api/loans/{id}/return", loan.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("RETURNED"));
    }

    @Test
    void shouldUpdateLoan() throws Exception {
        // Arrange
        LoanUpdateRequest request = new LoanUpdateRequest(LocalDateTime.now().plusDays(30));

        // Act & Assert
        mockMvc.perform(put("/api/loans/{id}", loan.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ACTIVE"));
    }

    @Test
    void shouldGetLoansByMember() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/loans/member/{memberId}", member.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)));
    }

    @Test
    void shouldGetLoansByStatus() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/loans/status/{status}", LoanStatus.ACTIVE))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)));
    }
}
