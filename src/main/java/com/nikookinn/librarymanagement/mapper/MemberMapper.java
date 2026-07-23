package com.nikookinn.librarymanagement.mapper;

import com.nikookinn.librarymanagement.dto.response.MemberResponse;
import com.nikookinn.librarymanagement.entity.Member;

public final class MemberMapper {
    private MemberMapper() {
    }

    public static MemberResponse toResponse(Member member) {
        return new MemberResponse(member.getId(), member.getFirstName(), member.getLastName(),
                member.getEmail(), member.getPhone(), member.getMembershipDate());
    }
}
