package com.nikookinn.librarymanagement.service.impl;

import com.nikookinn.librarymanagement.dto.request.AuthorCreateRequest;
import com.nikookinn.librarymanagement.dto.response.AuthorResponse;
import com.nikookinn.librarymanagement.dto.request.AuthorUpdateRequest;
import com.nikookinn.librarymanagement.entity.Author;
import com.nikookinn.librarymanagement.exception.ResourceNotFoundException;
import com.nikookinn.librarymanagement.mapper.AuthorMapper;
import com.nikookinn.librarymanagement.repository.AuthorRepository;
import com.nikookinn.librarymanagement.service.AuthorService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class AuthorServiceImpl implements AuthorService {
    private final AuthorRepository authorRepository;

    public AuthorServiceImpl(AuthorRepository authorRepository) {
        this.authorRepository = authorRepository;
    }

    @Override
    public Page<AuthorResponse> getAllAuthors(Pageable pageable) {
        return authorRepository.findAll(pageable)
                .map(AuthorMapper::toResponse);
    }

    @Override
    public AuthorResponse getAuthorById(Long id) {
        return authorRepository.findById(id)
                .map(AuthorMapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Author not found with id: " + id));
    }

    @Override
    public AuthorResponse createAuthor(AuthorCreateRequest request) {
        Author author = new Author();
        author.setFirstName(request.firstName());
        author.setLastName(request.lastName());
        author.setBirthDate(request.birthDate());
        author.setNationality(request.nationality());
        author.setBiography(request.biography());

        Author saved = authorRepository.save(author);
        return AuthorMapper.toResponse(saved);
    }

    @Override
    public AuthorResponse updateAuthor(Long id, AuthorUpdateRequest request) {
        Author author = authorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Author not found with id: " + id));

        author.setFirstName(request.firstName());
        author.setLastName(request.lastName());
        author.setBirthDate(request.birthDate());
        author.setNationality(request.nationality());
        author.setBiography(request.biography());

        Author updated = authorRepository.save(author);
        return AuthorMapper.toResponse(updated);
    }

    @Override
    public void deleteAuthor(Long id) {
        if (!authorRepository.existsById(id)) {
            throw new ResourceNotFoundException("Author not found with id: " + id);
        }
        authorRepository.deleteById(id);
    }

    @Override
    public Page<AuthorResponse> searchAuthors(String name, Pageable pageable) {
        return authorRepository.findByNameContainingIgnoreCase(name, pageable)
                .map(AuthorMapper::toResponse);
    }
}
