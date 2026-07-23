package com.nikookinn.librarymanagement.dto.request;

import jakarta.validation.constraints.NotBlank;

public record CategoryUpdateRequest(
        @NotBlank(message = "Category name cannot be blank")
        String name,

        String description) {
}
