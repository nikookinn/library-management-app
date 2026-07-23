package com.nikookinn.librarymanagement.mapper;

import com.nikookinn.librarymanagement.dto.response.AuthorResponse;
import com.nikookinn.librarymanagement.entity.Author;

public final class AuthorMapper {
    private AuthorMapper() {
    }

    public static AuthorResponse toResponse(Author author) {
        return new AuthorResponse(author.getId(), author.getFirstName(), author.getLastName(),
                author.getBirthDate(), author.getNationality(), author.getBiography());
    }
}
