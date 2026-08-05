package com.nikookinn.librarymanagement.service;

import com.nikookinn.librarymanagement.dto.request.MemberCreateRequest;
import com.nikookinn.librarymanagement.dto.response.MemberResponse;
import com.nikookinn.librarymanagement.dto.request.MemberUpdateRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface MemberService {
    Page<MemberResponse> getAllMembers(Pageable pageable);
    MemberResponse getMemberById(Long id);
    MemberResponse createMember(MemberCreateRequest request);
    MemberResponse updateMember(Long id, MemberUpdateRequest request);
    void deleteMember(Long id);
    
    Page<MemberResponse> searchMembersByName(String name, Pageable pageable);
    Page<MemberResponse> searchMembersByEmail(String email, Pageable pageable);
    List<MemberResponse> getMembersWithOverdueLoans();
}
