package com.nikookinn.librarymanagement.repository;

import com.nikookinn.librarymanagement.entity.Loan;
import com.nikookinn.librarymanagement.entity.LoanStatus;
import com.nikookinn.librarymanagement.dto.response.OverdueLoanResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface LoanRepository extends JpaRepository<Loan, Long> {
    Page<Loan> findByMember_Id(Long memberId, Pageable pageable);
    
    Page<Loan> findByMember_IdAndStatus(Long memberId, LoanStatus status, Pageable pageable);
    
    Page<Loan> findByStatus(LoanStatus status, Pageable pageable);

    long countByBook_IdAndStatusIn(Long bookId, java.util.Collection<LoanStatus> statuses);

    @Modifying(clearAutomatically = true)
    @Query("UPDATE Loan l SET l.status = 'OVERDUE' WHERE l.status = 'ACTIVE' AND l.dueDate < :now")
    int markOverdueLoans(@Param("now") LocalDateTime now);

    @Query("SELECT l FROM Loan l WHERE l.status = 'ACTIVE' AND l.dueDate < :currentDate ORDER BY l.dueDate ASC")
    List<Loan> findOverdueLoans(@Param("currentDate") LocalDateTime currentDate, Pageable pageable);

    @Query("SELECT l FROM Loan l JOIN FETCH l.book JOIN FETCH l.member WHERE l.member.id = :memberId AND l.status = :status")
    List<Loan> findLoansByMemberWithDetails(@Param("memberId") Long memberId, @Param("status") LoanStatus status);

    @Query("SELECT COUNT(l) FROM Loan l WHERE l.member.id = :memberId AND l.status IN ('ACTIVE', 'OVERDUE')")
    long countActiveLoansByMember(@Param("memberId") Long memberId);

    @Query("SELECT l FROM Loan l WHERE l.book.id = :bookId AND l.status IN ('ACTIVE', 'OVERDUE') ORDER BY l.borrowDate DESC")
    List<Loan> findActiveLoansByBook(@Param("bookId") Long bookId);

    @Query("SELECT new com.nikookinn.librarymanagement.dto.response.OverdueLoanResponse(" +
            "l.id, CONCAT(m.firstName, ' ', m.lastName), b.title, l.dueDate, 0L) " +
            "FROM Loan l JOIN l.member m JOIN l.book b " +
            "WHERE l.status = 'OVERDUE'")
    List<OverdueLoanResponse> findOverdueDetails();
}
