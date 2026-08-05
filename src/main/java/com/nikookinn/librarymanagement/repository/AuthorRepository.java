package com.nikookinn.librarymanagement.repository;

import com.nikookinn.librarymanagement.entity.Author;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface AuthorRepository extends JpaRepository<Author, Long> {
    @Query("SELECT a FROM Author a WHERE LOWER(CONCAT(a.firstName, ' ', a.lastName)) LIKE LOWER(CONCAT('%', :name, '%'))")
    Page<Author> findByNameContainingIgnoreCase(@Param("name") String name, Pageable pageable);

    @Query("SELECT DISTINCT a FROM Author a JOIN FETCH a.books WHERE SIZE(a.books) > 0 ORDER BY a.lastName ASC")
    List<Author> findAuthorsWithBooks();

    @Query("SELECT a FROM Author a WHERE a.nationality = :nationality ORDER BY a.lastName ASC")
    List<Author> findByNationality(@Param("nationality") String nationality);

    @Query(value = "SELECT a.id, a.first_name, a.last_name, COUNT(b.id) AS book_count FROM authors a LEFT JOIN book_authors ba ON a.id = ba.author_id LEFT JOIN books b ON ba.book_id = b.id GROUP BY a.id, a.first_name, a.last_name HAVING COUNT(b.id) >= :minBooks ORDER BY book_count DESC", nativeQuery = true)
    List<Object[]> findProlificsAuthors(@Param("minBooks") int minBooks);

    @Query("SELECT DISTINCT a FROM Author a JOIN a.books b WHERE b.category.name = :categoryName")
    List<Author> findAuthorsByCategoryName(@Param("categoryName") String categoryName);

    @Query("SELECT a FROM Author a JOIN a.books b GROUP BY a.id, a.firstName, a.lastName HAVING COUNT(DISTINCT b.category.id) > 1")
    List<Author> findMultiCategoryAuthors();
}
