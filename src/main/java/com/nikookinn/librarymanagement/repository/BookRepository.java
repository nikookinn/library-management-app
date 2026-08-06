package com.nikookinn.librarymanagement.repository;

import com.nikookinn.librarymanagement.entity.Book;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface BookRepository extends JpaRepository<Book, Long>, JpaSpecificationExecutor<Book> {
    @EntityGraph(value = "Book.category", type = EntityGraph.EntityGraphType.FETCH)
    Page<Book> findAll(Pageable pageable);

    @Override
    @EntityGraph(value = "Book.category", type = EntityGraph.EntityGraphType.FETCH)
    Page<Book> findAll(org.springframework.data.jpa.domain.Specification<Book> spec, Pageable pageable);

    @EntityGraph(value = "Book.category", type = EntityGraph.EntityGraphType.FETCH)
    Page<Book> findByCategory_Id(Long categoryId, Pageable pageable);
    
    @EntityGraph(value = "Book.category", type = EntityGraph.EntityGraphType.FETCH)
    @Query("SELECT b FROM Book b JOIN b.authors a WHERE a.id = :authorId")
    Page<Book> findByAuthor_Id(@Param("authorId") Long authorId, Pageable pageable);
    
    @EntityGraph(value = "Book.category", type = EntityGraph.EntityGraphType.FETCH)
    Page<Book> findByTitleContainingIgnoreCase(String title, Pageable pageable);
    
    @EntityGraph(value = "Book.category", type = EntityGraph.EntityGraphType.FETCH)
    Page<Book> findByAvailableCopiesGreaterThan(Integer copies, Pageable pageable);

    @Query("SELECT DISTINCT b FROM Book b JOIN FETCH b.authors JOIN FETCH b.category WHERE b.availableCopies > 0 ORDER BY b.title ASC")
    List<Book> findAvailableBooksWithDetails(Pageable pageable);

    @Query("SELECT b FROM Book b WHERE b.category.id = :categoryId AND b.availableCopies > :minCopies ORDER BY b.title ASC")
    List<Book> findByCategoryAndAvailability(@Param("categoryId") Long categoryId, @Param("minCopies") Integer minCopies);

    @Query(value = "SELECT b.id, b.title, b.isbn, COUNT(l.id) AS loan_count FROM books b LEFT JOIN loans l ON b.id = l.book_id AND l.status IN ('ACTIVE', 'OVERDUE') GROUP BY b.id, b.title, b.isbn ORDER BY loan_count DESC LIMIT :limit", nativeQuery = true)
    List<Object[]> findMostBorrowedBooks(@Param("limit") int limit);

    @EntityGraph(value = "Book.category", type = EntityGraph.EntityGraphType.FETCH)
    @Query("SELECT b FROM Book b WHERE LOWER(b.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(b.description) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<Book> searchByTitleOrDescription(@Param("keyword") String keyword, Pageable pageable);

    @Query(value = "SELECT b.* FROM books b WHERE b.id NOT IN (SELECT DISTINCT book_id FROM loans)", nativeQuery = true)
    List<Book> findBooksNeverBorrowed();

    @Query(value = "SELECT c.name, COUNT(l.id) as loan_count " +
            "FROM categories c " +
            "JOIN books b ON c.id = b.category_id " +
            "JOIN loans l ON b.id = l.book_id " +
            "GROUP BY c.id, c.name " +
            "ORDER BY loan_count DESC", nativeQuery = true)
    List<Object[]> findTopCategoriesByLoans();
}
