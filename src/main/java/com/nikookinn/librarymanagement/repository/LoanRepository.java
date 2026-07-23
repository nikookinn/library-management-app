package com.nikookinn.librarymanagement.repository;

import com.nikookinn.librarymanagement.entity.Loan;
import com.nikookinn.librarymanagement.entity.LoanStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;

public interface LoanRepository extends JpaRepository<Loan, Long> {
    Page<Loan> findByMember_Id(Long memberId, Pageable pageable);
    
    Page<Loan> findByMember_IdAndStatus(Long memberId, LoanStatus status, Pageable pageable);
    
    Page<Loan> findByStatus(LoanStatus status, Pageable pageable);

    long countByBook_IdAndStatusIn(Long bookId, java.util.Collection<LoanStatus> statuses);

    @Modifying(clearAutomatically = true)
    @Query("UPDATE Loan l SET l.status = 'OVERDUE' WHERE l.status = 'ACTIVE' AND l.dueDate < :now")
    int markOverdueLoans(@Param("now") LocalDateTime now);
}
