package com.nikookinn.librarymanagement.service;

import com.nikookinn.librarymanagement.dto.request.AuthorCreateRequest;
import com.nikookinn.librarymanagement.dto.response.AuthorResponse;
import com.nikookinn.librarymanagement.dto.request.AuthorUpdateRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface AuthorService {
    Page<AuthorResponse> getAllAuthors(Pageable pageable);
    AuthorResponse getAuthorById(Long id);
    AuthorResponse createAuthor(AuthorCreateRequest request);
    AuthorResponse updateAuthor(Long id, AuthorUpdateRequest request);
    void deleteAuthor(Long id);
    
    Page<AuthorResponse> searchAuthors(String name, Pageable pageable);
}
