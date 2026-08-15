package com.nikookinn.librarymanagement.repository;

import com.nikookinn.librarymanagement.dto.request.BookSearchRequest;
import com.nikookinn.librarymanagement.entity.*;
import com.nikookinn.librarymanagement.repository.specification.BookSpecification;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
@DisplayName("Book Repository Complex Query & Specification Tests")
class BookRepositoryIntegrationTest {

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private AuthorRepository authorRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private LoanRepository loanRepository;

    @Autowired
    private MemberRepository memberRepository;

    private Book book1;
    private Book book2;
    private Category fiction;
    private Category history;

    @BeforeEach
    void setUp() {
        loanRepository.deleteAll();
        bookRepository.deleteAll();
        authorRepository.deleteAll();
        categoryRepository.deleteAll();
        memberRepository.deleteAll();

        fiction = new Category();
        fiction.setName("Fiction");
        categoryRepository.save(fiction);

        history = new Category();
        history.setName("History");
        categoryRepository.save(history);

        Author tolkien = new Author();
        tolkien.setFirstName("J.R.R.");
        tolkien.setLastName("Tolkien");
        authorRepository.save(tolkien);

        Author yuval = new Author();
        yuval.setFirstName("Yuval Noah");
        yuval.setLastName("Harari");
        authorRepository.save(yuval);

        book1 = new Book();
        book1.setTitle("The Lord of the Rings");
        book1.setIsbn("ISBN-LOTR");
        book1.setCategory(fiction);
        book1.setAuthors(Set.of(tolkien));
        book1.setTotalCopies(10);
        book1.setAvailableCopies(5);
        book1.setPublishYear(1954);
        bookRepository.save(book1);

        book2 = new Book();
        book2.setTitle("Sapiens");
        book2.setIsbn("ISBN-SAPIENS");
        book2.setCategory(history);
        book2.setAuthors(Set.of(yuval));
        book2.setTotalCopies(10);
        book2.setAvailableCopies(10);
        book2.setPublishYear(2011);
        bookRepository.save(book2);

        Member member = new Member();
        member.setFirstName("John");
        member.setLastName("Doe");
        member.setEmail("john@example.com");
        member.setMembershipDate(LocalDate.now());
        memberRepository.save(member);


        Loan loan1 = new Loan();
        loan1.setBook(book1);
        loan1.setMember(member);
        loan1.setBorrowDate(LocalDateTime.now());
        loan1.setDueDate(LocalDateTime.now().plusDays(14));
        loan1.setStatus(LoanStatus.ACTIVE);
        loanRepository.save(loan1);
    }

    @Test
    @DisplayName("should filter books using Specification (Title and Author)")
    void shouldFilterBooksUsingSpecification() {
        // Arrange
        BookSearchRequest request = new BookSearchRequest("Lord", "Tolkien", null, null, null, null);

        // Act
        Page<Book> result = bookRepository.findAll(BookSpecification.filterByRequest(request), PageRequest.of(0, 10));

        // Assert
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().getFirst().getTitle()).isEqualTo("The Lord of the Rings");
    }

    @Test
    @DisplayName("should find most borrowed books")
    void shouldFindMostBorrowedBooks() {
        // Act
        List<Object[]> result = bookRepository.findMostBorrowedBooks(5);

        // Assert
        assertThat(result).isNotEmpty();
        // First element should be LOTR
        assertThat(result.getFirst()[1].toString()).isEqualTo("The Lord of the Rings");
        assertThat(((Number) result.getFirst()[3]).intValue()).isEqualTo(1);
    }

    @Test
    @DisplayName("should find top categories by loans")
    void shouldFindTopCategoriesByLoans() {
        // Act
        List<Object[]> result = bookRepository.findTopCategoriesByLoans();

        // Assert
        assertThat(result).isNotEmpty();
        // Fiction should be top because of LOTR loan
        assertThat(result.getFirst()[0].toString()).isEqualTo("Fiction");
        assertThat(((Number) result.getFirst()[1]).intValue()).isEqualTo(1);
    }

    @Test
    @DisplayName("should find books never borrowed")
    void shouldFindBooksNeverBorrowed() {
        // Act
        List<Book> result = bookRepository.findBooksNeverBorrowed();

        // Assert
        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getTitle()).isEqualTo("Sapiens");
    }

    @Test
    @DisplayName("should find available books with details")
    void shouldFindAvailableBooksWithDetails() {
        // Act
        Page<Book> result = bookRepository.findAvailableBooksWithDetails(PageRequest.of(0, 10));

        // Assert
        assertThat(result.getContent()).hasSize(2);
        // Verify titles to ensure sorting
        assertThat(result.getContent().get(0).getTitle()).isEqualTo("Sapiens");
        assertThat(result.getContent().get(1).getTitle()).isEqualTo("The Lord of the Rings");
    }
}
