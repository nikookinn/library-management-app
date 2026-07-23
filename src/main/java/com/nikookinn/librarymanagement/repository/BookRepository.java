package com.nikookinn.librarymanagement.repository;

import com.nikookinn.librarymanagement.entity.Book;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface BookRepository extends JpaRepository<Book, Long> {
    Page<Book> findByCategory_Id(Long categoryId, Pageable pageable);
    
    @Query("SELECT b FROM Book b JOIN b.authors a WHERE a.id = :authorId")
    Page<Book> findByAuthor_Id(@Param("authorId") Long authorId, Pageable pageable);
    
    Page<Book> findByTitleContainingIgnoreCase(String title, Pageable pageable);
    
    Page<Book> findByAvailableCopiesGreaterThan(Integer copies, Pageable pageable);
}
