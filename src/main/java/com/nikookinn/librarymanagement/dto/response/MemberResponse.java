package com.nikookinn.librarymanagement.dto.response;

import java.time.LocalDate;

public record MemberResponse(Long id, String firstName, String lastName, String email, String phone,
                             LocalDate membershipDate) {
}
