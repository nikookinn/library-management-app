package com.nikookinn.librarymanagement.service.impl;

import com.nikookinn.librarymanagement.dto.request.MemberCreateRequest;
import com.nikookinn.librarymanagement.dto.request.MemberUpdateRequest;
import com.nikookinn.librarymanagement.entity.Member;
import com.nikookinn.librarymanagement.repository.MemberRepository;
import com.nikookinn.librarymanagement.service.MemberService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;

@SpringBootTest
@DisplayName("Member Service Integration Tests")
class MemberServiceIntegrationTest {

    @Autowired
    private MemberService memberService;

    @MockitoSpyBean
    private MemberRepository memberRepository;

    @BeforeEach
    void setUp() {
        memberRepository.deleteAll();
    }

    @Test
    @DisplayName("should rollback member creation when save fails")
    void shouldRollbackMemberCreationWhenSaveFails() {
        MemberCreateRequest request = new MemberCreateRequest("John", "Doe", "john@example.com", "1234567890");
        
        doThrow(new RuntimeException("Simulated error")).when(memberRepository).save(any(Member.class));

        assertThatThrownBy(() -> memberService.createMember(request))
                .isInstanceOf(RuntimeException.class);

        assertThat(memberRepository.count()).isZero();
    }

    @Test
    @DisplayName("should rollback member update when save fails")
    void shouldRollbackMemberUpdateWhenSaveFails() {
        Member member = new Member();
        member.setFirstName("Original");
        member.setLastName("Member");
        member.setEmail("orig@example.com");
        member.setMembershipDate(LocalDate.now());
        final Member savedMember = memberRepository.save(member);

        MemberUpdateRequest request = new MemberUpdateRequest("Updated", "Name", "new@example.com", "9876543210");

        doThrow(new RuntimeException("Simulated error")).when(memberRepository).save(any(Member.class));

        assertThatThrownBy(() -> memberService.updateMember(savedMember.getId(), request))
                .isInstanceOf(RuntimeException.class);

        Member notUpdated = memberRepository.findById(savedMember.getId()).orElseThrow();
        assertThat(notUpdated.getFirstName()).isEqualTo("Original");
    }

    @Test
    @DisplayName("should rollback member deletion when delete fails")
    void shouldRollbackMemberDeletionWhenDeleteFails() {
        Member member = new Member();
        member.setFirstName("To Be Deleted");
        member.setLastName("Member");
        member.setEmail("delete@example.com");
        member.setMembershipDate(LocalDate.now());
        final Member memberToDelete = memberRepository.save(member);

        doThrow(new RuntimeException("Simulated error during delete"))
                .when(memberRepository).deleteById(any(Long.class));

        assertThatThrownBy(() -> memberService.deleteMember(memberToDelete.getId()))
                .isInstanceOf(RuntimeException.class);

        assertThat(memberRepository.existsById(memberToDelete.getId())).isTrue();
    }
}
