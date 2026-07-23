package com.nikookinn.librarymanagement.dto.request;

import jakarta.validation.constraints.NotBlank;

import java.time.LocalDate;

public record AuthorUpdateRequest(
        @NotBlank(message = "First name cannot be blank")
        String firstName,

        @NotBlank(message = "Last name cannot be blank")
        String lastName,

        LocalDate birthDate,

        String nationality,

        String biography) {
}
