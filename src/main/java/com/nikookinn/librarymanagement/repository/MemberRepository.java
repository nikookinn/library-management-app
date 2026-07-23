package com.nikookinn.librarymanagement.repository;

import com.nikookinn.librarymanagement.entity.Member;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MemberRepository extends JpaRepository<Member, Long> {
    @Query("SELECT m FROM Member m WHERE LOWER(CONCAT(m.firstName, ' ', m.lastName)) LIKE LOWER(CONCAT('%', :name, '%'))")
    Page<Member> findByNameContainingIgnoreCase(@Param("name") String name, Pageable pageable);
    
    Page<Member> findByEmailContainingIgnoreCase(String email, Pageable pageable);
}
