package com.nikookinn.librarymanagement.service.impl;

import com.nikookinn.librarymanagement.dto.request.LoanCreateRequest;
import com.nikookinn.librarymanagement.dto.response.LoanResponse;
import com.nikookinn.librarymanagement.entity.Book;
import com.nikookinn.librarymanagement.entity.Loan;
import com.nikookinn.librarymanagement.entity.LoanStatus;
import com.nikookinn.librarymanagement.entity.Member;
import com.nikookinn.librarymanagement.exception.BusinessRuleViolationException;
import com.nikookinn.librarymanagement.exception.ResourceNotFoundException;
import com.nikookinn.librarymanagement.repository.BookRepository;
import com.nikookinn.librarymanagement.repository.LoanRepository;
import com.nikookinn.librarymanagement.repository.MemberRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Loan Service Unit Tests")
class LoanServiceImplTest {

    @Mock
    private LoanRepository loanRepository;
    @Mock
    private BookRepository bookRepository;
    @Mock
    private MemberRepository memberRepository;

    @InjectMocks
    private LoanServiceImpl loanService;

    private Loan loan;
    private Book book;
    private Member member;

    @BeforeEach
    void setUp() {
        member = new Member();
        member.setId(1L);
        member.setFirstName("Frodo");
        member.setLastName("Baggins");

        book = new Book();
        book.setId(1L);
        book.setTitle("The Fellowship of the Ring");
        book.setTotalCopies(1);
        book.setAvailableCopies(1);

        loan = new Loan();
        loan.setId(1L);
        loan.setBook(book);
        loan.setMember(member);
        loan.setStatus(LoanStatus.ACTIVE);
        loan.setDueDate(LocalDateTime.now().plusDays(14));
    }

    @Nested
    @DisplayName("createLoan")
    class CreateLoan {
        @Test
        @DisplayName("should create loan and decrease book available copies")
        void shouldCreateLoan() {
            // Arrange
            LoanCreateRequest request = new LoanCreateRequest(1L, 1L, LocalDateTime.now().plusDays(14));
            when(bookRepository.findById(1L)).thenReturn(Optional.of(book));
            when(memberRepository.findById(1L)).thenReturn(Optional.of(member));
            when(loanRepository.save(any(Loan.class))).thenReturn(loan);

            // Act
            LoanResponse result = loanService.createLoan(request);

            // Assert
            assertThat(result.status()).isEqualTo(LoanStatus.ACTIVE);
            assertThat(book.getAvailableCopies()).isZero();
            verify(bookRepository).findById(1L);
            verify(memberRepository).findById(1L);
            verify(bookRepository).save(book);
            verify(loanRepository).save(any(Loan.class));
        }

        @Test
        @DisplayName("should throw ResourceNotFoundException when book not found")
        void shouldThrowExceptionWhenBookNotFound() {
            // Arrange
            LoanCreateRequest request = new LoanCreateRequest(1L, 1L, LocalDateTime.now().plusDays(14));
            when(bookRepository.findById(1L)).thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> loanService.createLoan(request))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Book not found");

            verify(bookRepository).findById(1L);
            verify(memberRepository, never()).findById(anyLong());
            verify(bookRepository, never()).save(any(Book.class));
            verify(loanRepository, never()).save(any(Loan.class));
        }

        @Test
        @DisplayName("should throw ResourceNotFoundException when member not found")
        void shouldThrowExceptionWhenMemberNotFound() {
            // Arrange
            LoanCreateRequest request = new LoanCreateRequest(1L, 1L, LocalDateTime.now().plusDays(14));
            when(bookRepository.findById(1L)).thenReturn(Optional.of(book));
            when(memberRepository.findById(1L)).thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> loanService.createLoan(request))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Member not found");

            verify(bookRepository).findById(1L);
            verify(memberRepository).findById(1L);
            verify(bookRepository, never()).save(any(Book.class));
            verify(loanRepository, never()).save(any(Loan.class));
        }

        @Test
        @DisplayName("should throw BusinessRuleViolationException when book is not available")
        void shouldThrowExceptionWhenBookNotAvailableForLoan() {
            // Arrange
            book.setAvailableCopies(0);
            LoanCreateRequest request = new LoanCreateRequest(1L, 1L, LocalDateTime.now().plusDays(14));
            when(bookRepository.findById(1L)).thenReturn(Optional.of(book));
            when(memberRepository.findById(1L)).thenReturn(Optional.of(member));

            // Act & Assert
            assertThatThrownBy(() -> loanService.createLoan(request))
                    .isInstanceOf(BusinessRuleViolationException.class)
                    .hasMessageContaining("Book is not available");

            verify(bookRepository).findById(1L);
            verify(memberRepository).findById(1L);
            verify(bookRepository, never()).save(any(Book.class));
            verify(loanRepository, never()).save(any(Loan.class));
        }
    }

    @Nested
    @DisplayName("returnLoan")
    class ReturnLoan {
        @Test
        @DisplayName("should return loan and increase book available copies")
        void shouldReturnLoan() {
            // Arrange
            book.setAvailableCopies(0);
            book.setTotalCopies(1);
            when(loanRepository.findById(1L)).thenReturn(Optional.of(loan));
            when(loanRepository.save(any(Loan.class))).thenReturn(loan);

            // Act
            LoanResponse result = loanService.returnLoan(1L);

            // Assert
            assertThat(loan.getStatus()).isEqualTo(LoanStatus.RETURNED);
            assertThat(book.getAvailableCopies()).isEqualTo(1);
            verify(loanRepository).markOverdueLoans(any());
            verify(loanRepository).findById(1L);
            verify(bookRepository).save(book);
            verify(loanRepository).save(loan);
        }

        @Test
        @DisplayName("should throw ResourceNotFoundException when loan not found")
        void shouldThrowExceptionWhenLoanNotFound() {
            // Act & Assert
            when(loanRepository.findById(1L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> loanService.returnLoan(1L))
                    .isInstanceOf(ResourceNotFoundException.class);

            verify(loanRepository).findById(1L);
            verify(bookRepository, never()).save(any(Book.class));
            verify(loanRepository, never()).save(any(Loan.class));
        }

        @Test
        @DisplayName("should throw BusinessRuleViolationException when loan is already returned")
        void shouldThrowExceptionWhenReturningAlreadyReturnedLoan() {
            // Arrange
            loan.setStatus(LoanStatus.RETURNED);
            when(loanRepository.findById(1L)).thenReturn(Optional.of(loan));

            // Act & Assert
            assertThatThrownBy(() -> loanService.returnLoan(1L))
                    .isInstanceOf(BusinessRuleViolationException.class)
                    .hasMessageContaining("already been returned");

            verify(loanRepository).findById(1L);
            verify(bookRepository, never()).save(any(Book.class));
            verify(loanRepository, never()).save(any(Loan.class));
        }

        @Test
        @DisplayName("should throw BusinessRuleViolationException when inventory is inconsistent")
        void shouldThrowExceptionWhenInventoryInconsistentDuringReturn() {
            // Arrange
            book.setAvailableCopies(1);
            book.setTotalCopies(1); // Already full
            when(loanRepository.findById(1L)).thenReturn(Optional.of(loan));

            // Act & Assert
            assertThatThrownBy(() -> loanService.returnLoan(1L))
                    .isInstanceOf(BusinessRuleViolationException.class)
                    .hasMessageContaining("inventory is inconsistent");

            verify(loanRepository).findById(1L);
            verify(bookRepository, never()).save(any(Book.class));
            verify(loanRepository, never()).save(any(Loan.class));
        }
    }
}
