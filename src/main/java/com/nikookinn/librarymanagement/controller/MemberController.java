package com.nikookinn.librarymanagement.controller;

import com.nikookinn.librarymanagement.controller.api.MemberApi;
import com.nikookinn.librarymanagement.dto.request.MemberCreateRequest;
import com.nikookinn.librarymanagement.dto.response.MemberResponse;
import com.nikookinn.librarymanagement.dto.request.MemberUpdateRequest;
import com.nikookinn.librarymanagement.service.MemberService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/members")
public class MemberController implements MemberApi {
    private final MemberService memberService;

    public MemberController(MemberService memberService) {
        this.memberService = memberService;
    }

    @Override
    @GetMapping
    public ResponseEntity<Page<MemberResponse>> getAllMembers(
            @PageableDefault(size = 10, page = 0) Pageable pageable) {
        Page<MemberResponse> members = memberService.getAllMembers(pageable);
        return ResponseEntity.ok(members);
    }

    @Override
    @GetMapping("/{id}")
    public ResponseEntity<MemberResponse> getMemberById(@PathVariable Long id) {
        MemberResponse member = memberService.getMemberById(id);
        return ResponseEntity.ok(member);
    }

    @Override
    @PostMapping
    public ResponseEntity<MemberResponse> createMember(@Valid @RequestBody MemberCreateRequest request) {
        MemberResponse created = memberService.createMember(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @Override
    @PutMapping("/{id}")
    public ResponseEntity<MemberResponse> updateMember(
            @PathVariable Long id,
            @Valid @RequestBody MemberUpdateRequest request) {
        MemberResponse updated = memberService.updateMember(id, request);
        return ResponseEntity.ok(updated);
    }

    @Override
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMember(@PathVariable Long id) {
        memberService.deleteMember(id);
        return ResponseEntity.noContent().build();
    }

    @Override
    @GetMapping("/search/name")
    public ResponseEntity<Page<MemberResponse>> searchByName(
            @RequestParam String name,
            @PageableDefault(size = 10, page = 0) Pageable pageable) {
        Page<MemberResponse> members = memberService.searchMembersByName(name, pageable);
        return ResponseEntity.ok(members);
    }

    @Override
    @GetMapping("/search/email")
    public ResponseEntity<Page<MemberResponse>> searchByEmail(
            @RequestParam String email,
            @PageableDefault(size = 10, page = 0) Pageable pageable) {
        Page<MemberResponse> members = memberService.searchMembersByEmail(email, pageable);
        return ResponseEntity.ok(members);
    }
}
