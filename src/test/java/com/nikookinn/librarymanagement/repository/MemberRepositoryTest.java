package com.nikookinn.librarymanagement.repository;

import com.nikookinn.librarymanagement.entity.Member;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class MemberRepositoryTest {

    @Autowired
    private MemberRepository memberRepository;

    private Member member1;
    private Member member2;

    @BeforeEach
    void setUp() {
        member1 = new Member();
        member1.setFirstName("Frodo");
        member1.setLastName("Baggins");
        member1.setEmail("frodo@shire.com");
        member1.setMembershipDate(LocalDate.now());
        memberRepository.save(member1);

        member2 = new Member();
        member2.setFirstName("Harry");
        member2.setLastName("Potter");
        member2.setEmail("harry@hogwarts.com");
        member2.setMembershipDate(LocalDate.now());
        memberRepository.save(member2);
    }

    @Test
    void shouldFindMemberByFullNameContainingIgnoreCase() {
        // Arrange
        String searchTerm = "frodo";
        Pageable pageable = PageRequest.of(0, 10);

        // Act
        Page<Member> result = memberRepository.findByNameContainingIgnoreCase(searchTerm, pageable);

        // Assert
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().getFirst().getFirstName()).isEqualTo("Frodo");
    }

    @Test
    void shouldFindMemberByEmailContainingIgnoreCase() {
        // Arrange
        String searchTerm = "hogwarts.com";
        Pageable pageable = PageRequest.of(0, 10);

        // Act
        Page<Member> result = memberRepository.findByEmailContainingIgnoreCase(searchTerm, pageable);

        // Assert
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getEmail()).isEqualTo("harry@hogwarts.com");
    }

    @Test
    void shouldReturnEmptyPageWhenMemberNotFound() {
        // Arrange
        String searchTerm = "notfound";
        Pageable pageable = PageRequest.of(0, 10);

        // Act
        Page<Member> result = memberRepository.findByNameContainingIgnoreCase(searchTerm, pageable);

        // Assert
        assertThat(result.getContent()).isEmpty();
    }
}
