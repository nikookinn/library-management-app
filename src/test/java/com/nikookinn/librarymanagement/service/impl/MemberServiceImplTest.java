package com.nikookinn.librarymanagement.service.impl;

import com.nikookinn.librarymanagement.dto.request.MemberCreateRequest;
import com.nikookinn.librarymanagement.dto.request.MemberUpdateRequest;
import com.nikookinn.librarymanagement.dto.response.MemberResponse;
import com.nikookinn.librarymanagement.entity.Member;
import com.nikookinn.librarymanagement.exception.ResourceNotFoundException;
import com.nikookinn.librarymanagement.repository.MemberRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Member Service Unit Tests")
class MemberServiceImplTest {

    @Mock
    private MemberRepository memberRepository;

    @InjectMocks
    private MemberServiceImpl memberService;

    private Member member;

    @BeforeEach
    void setUp() {
        member = new Member();
        member.setId(1L);
        member.setFirstName("Frodo");
        member.setLastName("Baggins");
        member.setEmail("frodo@shire.com");
    }

    @Nested
    @DisplayName("getAllMembers")
    class GetAllMembers {
        @Test
        @DisplayName("should return all members with pagination")
        void shouldGetAllMembers() {
            // Arrange
            Pageable pageable = PageRequest.of(0, 10);
            Page<Member> memberPage = new PageImpl<>(List.of(member));
            when(memberRepository.findAll(pageable)).thenReturn(memberPage);

            // Act
            Page<MemberResponse> result = memberService.getAllMembers(pageable);

            // Assert
            assertThat(result.getContent()).hasSize(1);
            verify(memberRepository).findAll(pageable);
        }
    }

    @Nested
    @DisplayName("createMember")
    class CreateMember {
        @Test
        @DisplayName("should create and return member")
        void shouldCreateMember() {
            // Arrange
            MemberCreateRequest request = new MemberCreateRequest("Frodo", "Baggins", "frodo@shire.com", "123456");
            when(memberRepository.save(any(Member.class))).thenReturn(member);

            // Act
            MemberResponse result = memberService.createMember(request);

            // Assert
            assertThat(result.email()).isEqualTo("frodo@shire.com");
            verify(memberRepository).save(any(Member.class));
        }
    }

    @Nested
    @DisplayName("getMemberById")
    class GetMemberById {
        @Test
        @DisplayName("should return member when valid ID is provided")
        void shouldGetMemberById() {
            // Arrange
            when(memberRepository.findById(1L)).thenReturn(Optional.of(member));

            // Act
            MemberResponse result = memberService.getMemberById(1L);

            // Assert
            assertThat(result.firstName()).isEqualTo("Frodo");
            verify(memberRepository).findById(1L);
        }

        @Test
        @DisplayName("should throw ResourceNotFoundException when member not found")
        void shouldThrowExceptionWhenMemberNotFound() {
            // Arrange
            when(memberRepository.findById(1L)).thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> memberService.getMemberById(1L))
                    .isInstanceOf(ResourceNotFoundException.class);
            verify(memberRepository).findById(1L);
        }
    }

    @Nested
    @DisplayName("updateMember")
    class UpdateMember {
        @Test
        @DisplayName("should update and return member")
        void shouldUpdateMember() {
            // Arrange
            MemberUpdateRequest request = new MemberUpdateRequest("Samwise", "Gamgee", "sam@shire.com", "654321");
            when(memberRepository.findById(1L)).thenReturn(Optional.of(member));
            when(memberRepository.save(any(Member.class))).thenReturn(member);

            // Act
            MemberResponse result = memberService.updateMember(1L, request);

            // Assert
            assertThat(result.firstName()).isEqualTo("Samwise");
            verify(memberRepository).findById(1L);
            verify(memberRepository).save(any(Member.class));
        }

        @Test
        @DisplayName("should throw ResourceNotFoundException when member not found during update")
        void shouldThrowExceptionWhenMemberNotFoundDuringUpdate() {
            // Arrange
            MemberUpdateRequest request = new MemberUpdateRequest("Sam", "Gamgee", "sam@shire.com", "654321");
            when(memberRepository.findById(1L)).thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> memberService.updateMember(1L, request))
                    .isInstanceOf(ResourceNotFoundException.class);

            verify(memberRepository).findById(1L);
            verify(memberRepository, never()).save(any(Member.class));
        }
    }

    @Nested
    @DisplayName("deleteMember")
    class DeleteMember {
        @Test
        @DisplayName("should delete member when it exists")
        void shouldDeleteMember() {
            // Arrange
            when(memberRepository.existsById(1L)).thenReturn(true);

            // Act
            memberService.deleteMember(1L);

            // Assert
            verify(memberRepository).existsById(1L);
            verify(memberRepository).deleteById(1L);
        }

        @Test
        @DisplayName("should throw ResourceNotFoundException when member not found during deletion")
        void shouldThrowExceptionWhenMemberNotFoundDuringDeletion() {
            // Arrange
            when(memberRepository.existsById(1L)).thenReturn(false);

            // Act & Assert
            assertThatThrownBy(() -> memberService.deleteMember(1L))
                    .isInstanceOf(ResourceNotFoundException.class);

            verify(memberRepository).existsById(1L);
            verify(memberRepository, never()).deleteById(anyLong());
        }
    }
}
