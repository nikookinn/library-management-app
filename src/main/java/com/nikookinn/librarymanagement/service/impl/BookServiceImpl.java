package com.nikookinn.librarymanagement.service.impl;

import com.nikookinn.librarymanagement.dto.request.BookCreateRequest;
import com.nikookinn.librarymanagement.dto.request.BookSearchRequest;
import com.nikookinn.librarymanagement.dto.response.BookResponse;
import com.nikookinn.librarymanagement.dto.request.BookUpdateRequest;
import com.nikookinn.librarymanagement.dto.response.CategoryStatsResponse;
import com.nikookinn.librarymanagement.dto.response.BookLoanStatsResponse;
import com.nikookinn.librarymanagement.entity.Author;
import com.nikookinn.librarymanagement.entity.Book;
import com.nikookinn.librarymanagement.entity.Category;
import com.nikookinn.librarymanagement.entity.LoanStatus;
import com.nikookinn.librarymanagement.exception.BusinessRuleViolationException;
import com.nikookinn.librarymanagement.exception.ResourceNotFoundException;
import com.nikookinn.librarymanagement.mapper.BookMapper;
import com.nikookinn.librarymanagement.repository.AuthorRepository;
import com.nikookinn.librarymanagement.repository.BookRepository;
import com.nikookinn.librarymanagement.repository.CategoryRepository;
import com.nikookinn.librarymanagement.repository.LoanRepository;
import com.nikookinn.librarymanagement.repository.specification.BookSpecification;
import com.nikookinn.librarymanagement.service.BookService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class BookServiceImpl implements BookService {
    private final BookRepository bookRepository;
    private final CategoryRepository categoryRepository;
    private final AuthorRepository authorRepository;
    private final LoanRepository loanRepository;

    public BookServiceImpl(BookRepository bookRepository, CategoryRepository categoryRepository,
                           AuthorRepository authorRepository, LoanRepository loanRepository) {
        this.bookRepository = bookRepository;
        this.categoryRepository = categoryRepository;
        this.authorRepository = authorRepository;
        this.loanRepository = loanRepository;
    }

    @Override
    public Page<BookResponse> getAllBooks(Pageable pageable) {
        return bookRepository.findAll(pageable)
                .map(BookMapper::toResponse);
    }

    @Override
    public BookResponse getBookById(Long id) {
        return bookRepository.findById(id)
                .map(BookMapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Book not found with id: " + id));
    }

    @Override
    public BookResponse createBook(BookCreateRequest request) {
        Category category = categoryRepository.findById(request.categoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + request.categoryId()));

        Book book = new Book();
        book.setTitle(request.title());
        book.setIsbn(request.isbn());
        book.setPublishYear(request.publishYear());
        book.setDescription(request.description());
        book.setTotalCopies(request.totalCopies());
        book.setAvailableCopies(request.totalCopies());
        book.setCategory(category);
        book.setAuthors(new HashSet<>());

        Book saved = bookRepository.save(book);
        return BookMapper.toResponse(saved);
    }

    @Override
    public BookResponse updateBook(Long id, BookUpdateRequest request) {
        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Book not found with id: " + id));

        long borrowedCopies = loanRepository.countByBook_IdAndStatusIn(
                id, List.of(LoanStatus.ACTIVE, LoanStatus.OVERDUE));
        if (request.totalCopies() < borrowedCopies) {
            throw new BusinessRuleViolationException(
                    "Total copies cannot be less than the number of currently loaned copies");
        }

        Category category = categoryRepository.findById(request.categoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + request.categoryId()));

        book.setTitle(request.title());
        book.setIsbn(request.isbn());
        book.setPublishYear(request.publishYear());
        book.setDescription(request.description());
        book.setTotalCopies(request.totalCopies());
        book.setAvailableCopies(Math.toIntExact(request.totalCopies() - borrowedCopies));
        book.setCategory(category);

        Book updated = bookRepository.save(book);
        return BookMapper.toResponse(updated);
    }

    @Override
    public void deleteBook(Long id) {
        if (!bookRepository.existsById(id)) {
            throw new ResourceNotFoundException("Book not found with id: " + id);
        }
        bookRepository.deleteById(id);
    }

    @Override
    public Page<BookResponse> getBooksByCategory(Long categoryId, Pageable pageable) {
        categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + categoryId));
        
        return bookRepository.findByCategory_Id(categoryId, pageable)
                .map(BookMapper::toResponse);
    }

    @Override
    public Page<BookResponse> getBooksByAuthor(Long authorId, Pageable pageable) {
        authorRepository.findById(authorId)
                .orElseThrow(() -> new ResourceNotFoundException("Author not found with id: " + authorId));
        
        return bookRepository.findByAuthor_Id(authorId, pageable)
                .map(BookMapper::toResponse);
    }

    @Override
    public Page<BookResponse> searchBooks(String query, Pageable pageable) {
        return bookRepository.findByTitleContainingIgnoreCase(query, pageable)
                .map(BookMapper::toResponse);
    }

    @Override
    public Page<BookResponse> searchBooksDynamic(BookSearchRequest request, Pageable pageable) {
        return bookRepository.findAll(BookSpecification.filterByRequest(request), pageable)
                .map(BookMapper::toResponse);
    }

    @Override
    public Page<BookResponse> getAvailableBooks(Pageable pageable) {
        return bookRepository.findByAvailableCopiesGreaterThan(0, pageable)
                .map(BookMapper::toResponse);
    }

    @Override
    public void addAuthorToBook(Long bookId, Long authorId) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new ResourceNotFoundException("Book not found with id: " + bookId));
        
        Author author = authorRepository.findById(authorId)
                .orElseThrow(() -> new ResourceNotFoundException("Author not found with id: " + authorId));
        
        book.addAuthor(author);
        bookRepository.save(book);
    }

    @Override
    public void removeAuthorFromBook(Long bookId, Long authorId) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new ResourceNotFoundException("Book not found with id: " + bookId));
        
        Author author = authorRepository.findById(authorId)
                .orElseThrow(() -> new ResourceNotFoundException("Author not found with id: " + authorId));
        
        book.removeAuthor(author);
        bookRepository.save(book);
    }

    @Override
    public List<BookResponse> getBooksNeverBorrowed() {
        return bookRepository.findBooksNeverBorrowed()
                .stream()
                .map(BookMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<CategoryStatsResponse> getTopCategories() {
        return bookRepository.findTopCategoriesByLoans()
                .stream()
                .map(obj -> new CategoryStatsResponse((String) obj[0], ((Number) obj[1]).longValue()))
                .collect(Collectors.toList());
    }

    @Override
    public List<BookResponse> getAvailableBooksWithDetails(Pageable pageable) {
        return bookRepository.findAvailableBooksWithDetails(pageable)
                .stream()
                .map(BookMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<BookResponse> getBooksByCategoryAndAvailability(Long categoryId, int minCopies) {
        return bookRepository.findByCategoryAndAvailability(categoryId, minCopies)
                .stream()
                .map(BookMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<BookLoanStatsResponse> getMostBorrowedBooks(int limit) {
        return bookRepository.findMostBorrowedBooks(limit)
                .stream()
                .map(obj -> new BookLoanStatsResponse(
                        ((Number) obj[0]).longValue(),
                        (String) obj[1],
                        (String) obj[2],
                        ((Number) obj[3]).longValue()
                ))
                .collect(Collectors.toList());
    }

}
