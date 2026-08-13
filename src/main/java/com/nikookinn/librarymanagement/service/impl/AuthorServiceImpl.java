package com.nikookinn.librarymanagement.service.impl;

import com.nikookinn.librarymanagement.dto.request.AuthorCreateRequest;
import com.nikookinn.librarymanagement.dto.response.AuthorResponse;
import com.nikookinn.librarymanagement.dto.request.AuthorUpdateRequest;
import com.nikookinn.librarymanagement.dto.response.ProlificAuthorResponse;
import com.nikookinn.librarymanagement.entity.Author;
import com.nikookinn.librarymanagement.exception.ResourceNotFoundException;
import com.nikookinn.librarymanagement.mapper.AuthorMapper;
import com.nikookinn.librarymanagement.repository.AuthorRepository;
import com.nikookinn.librarymanagement.service.AuthorService;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class AuthorServiceImpl implements AuthorService {
    private final AuthorRepository authorRepository;

    public AuthorServiceImpl(AuthorRepository authorRepository) {
        this.authorRepository = authorRepository;
    }

    @Override
    @Cacheable(value = "authors", key = "#pageable")
    public Page<AuthorResponse> getAllAuthors(Pageable pageable) {
        return authorRepository.findAll(pageable)
                .map(AuthorMapper::toResponse);
    }

    @Override
    @Cacheable(value = "authors", key = "#id")
    public AuthorResponse getAuthorById(Long id) {
        return authorRepository.findById(id)
                .map(AuthorMapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Author not found with id: " + id));
    }

    @Override
    @Transactional
    @CacheEvict(value = "authors", allEntries = true)
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
    @Transactional
    @CacheEvict(value = "authors", allEntries = true)
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
    @Transactional
    @CacheEvict(value = "authors", allEntries = true)
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

    @Override
    public List<AuthorResponse> getAuthorsByCategory(String categoryName) {
        return authorRepository.findAuthorsByCategoryName(categoryName)
                .stream()
                .map(AuthorMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<AuthorResponse> getMultiCategoryAuthors() {
        return authorRepository.findMultiCategoryAuthors()
                .stream()
                .map(AuthorMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<AuthorResponse> getAuthorsWithBooks() {
        return authorRepository.findAuthorsWithBooks()
                .stream()
                .map(AuthorMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<AuthorResponse> getAuthorsByNationality(String nationality) {
        return authorRepository.findByNationality(nationality)
                .stream()
                .map(AuthorMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<ProlificAuthorResponse> getProlificAuthors(int minBooks) {
        return authorRepository.findProlificsAuthors(minBooks)
                .stream()
                .map(obj -> new ProlificAuthorResponse(
                        ((Number) obj[0]).longValue(),
                        (String) obj[1] + " " + (String) obj[2],
                        ((Number) obj[3]).longValue()
                ))
                .collect(Collectors.toList());
    }
}
