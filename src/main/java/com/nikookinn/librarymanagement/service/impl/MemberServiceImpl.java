package com.nikookinn.librarymanagement.service.impl;

import com.nikookinn.librarymanagement.dto.request.MemberCreateRequest;
import com.nikookinn.librarymanagement.dto.response.MemberResponse;
import com.nikookinn.librarymanagement.dto.request.MemberUpdateRequest;
import com.nikookinn.librarymanagement.entity.Member;
import com.nikookinn.librarymanagement.exception.ResourceNotFoundException;
import com.nikookinn.librarymanagement.mapper.MemberMapper;
import com.nikookinn.librarymanagement.repository.MemberRepository;
import com.nikookinn.librarymanagement.service.MemberService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import java.time.LocalDate;

@Service
public class MemberServiceImpl implements MemberService {
    private final MemberRepository memberRepository;

    public MemberServiceImpl(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    @Override
    public Page<MemberResponse> getAllMembers(Pageable pageable) {
        return memberRepository.findAll(pageable)
                .map(MemberMapper::toResponse);
    }

    @Override
    public MemberResponse getMemberById(Long id) {
        return memberRepository.findById(id)
                .map(MemberMapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Member not found with id: " + id));
    }

    @Override
    public MemberResponse createMember(MemberCreateRequest request) {
        Member member = new Member();
        member.setFirstName(request.firstName());
        member.setLastName(request.lastName());
        member.setEmail(request.email());
        member.setPhone(request.phone());
        member.setMembershipDate(LocalDate.now());

        Member saved = memberRepository.save(member);
        return MemberMapper.toResponse(saved);
    }

    @Override
    public MemberResponse updateMember(Long id, MemberUpdateRequest request) {
        Member member = memberRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Member not found with id: " + id));

        member.setFirstName(request.firstName());
        member.setLastName(request.lastName());
        member.setEmail(request.email());
        member.setPhone(request.phone());

        Member updated = memberRepository.save(member);
        return MemberMapper.toResponse(updated);
    }

    @Override
    public void deleteMember(Long id) {
        if (!memberRepository.existsById(id)) {
            throw new ResourceNotFoundException("Member not found with id: " + id);
        }
        memberRepository.deleteById(id);
    }

    @Override
    public Page<MemberResponse> searchMembersByName(String name, Pageable pageable) {
        return memberRepository.findByNameContainingIgnoreCase(name, pageable)
                .map(MemberMapper::toResponse);
    }

    @Override
    public Page<MemberResponse> searchMembersByEmail(String email, Pageable pageable) {
        return memberRepository.findByEmailContainingIgnoreCase(email, pageable)
                .map(MemberMapper::toResponse);
    }
}
