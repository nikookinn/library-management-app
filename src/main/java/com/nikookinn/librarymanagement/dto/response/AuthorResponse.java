package com.nikookinn.librarymanagement.dto.response;

import java.time.LocalDate;

public record AuthorResponse(Long id, String firstName, String lastName, LocalDate birthDate,
                             String nationality, String biography) {
}
