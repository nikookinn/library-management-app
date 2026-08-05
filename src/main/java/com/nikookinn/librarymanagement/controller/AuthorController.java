package com.nikookinn.librarymanagement.controller;

import com.nikookinn.librarymanagement.controller.api.AuthorApi;
import com.nikookinn.librarymanagement.dto.request.AuthorCreateRequest;
import com.nikookinn.librarymanagement.dto.response.AuthorResponse;
import com.nikookinn.librarymanagement.dto.request.AuthorUpdateRequest;
import com.nikookinn.librarymanagement.dto.response.ProlificAuthorResponse;
import com.nikookinn.librarymanagement.service.AuthorService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/authors")
public class AuthorController implements AuthorApi {
    private final AuthorService authorService;

    public AuthorController(AuthorService authorService) {
        this.authorService = authorService;
    }

    @Override
    @GetMapping
    public ResponseEntity<Page<AuthorResponse>> getAllAuthors(
            @PageableDefault(size = 10, page = 0) Pageable pageable) {
        Page<AuthorResponse> authors = authorService.getAllAuthors(pageable);
        return ResponseEntity.ok(authors);
    }

    @Override
    @GetMapping("/{id}")
    public ResponseEntity<AuthorResponse> getAuthorById(@PathVariable Long id) {
        AuthorResponse author = authorService.getAuthorById(id);
        return ResponseEntity.ok(author);
    }

    @Override
    @PostMapping
    public ResponseEntity<AuthorResponse> createAuthor(@Valid @RequestBody AuthorCreateRequest request) {
        AuthorResponse created = authorService.createAuthor(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @Override
    @PutMapping("/{id}")
    public ResponseEntity<AuthorResponse> updateAuthor(
            @PathVariable Long id,
            @Valid @RequestBody AuthorUpdateRequest request) {
        AuthorResponse updated = authorService.updateAuthor(id, request);
        return ResponseEntity.ok(updated);
    }

    @Override
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAuthor(@PathVariable Long id) {
        authorService.deleteAuthor(id);
        return ResponseEntity.noContent().build();
    }

    @Override
    @GetMapping("/search")
    public ResponseEntity<Page<AuthorResponse>> searchAuthors(
            @RequestParam String name,
            @PageableDefault(size = 10, page = 0) Pageable pageable) {
        Page<AuthorResponse> authors = authorService.searchAuthors(name, pageable);
        return ResponseEntity.ok(authors);
    }

    @Override
    @GetMapping("/by-category")
    public ResponseEntity<List<AuthorResponse>> getAuthorsByCategory(@RequestParam String categoryName) {
        return ResponseEntity.ok(authorService.getAuthorsByCategory(categoryName));
    }

    @Override
    @GetMapping("/multi-category")
    public ResponseEntity<List<AuthorResponse>> getMultiCategoryAuthors() {
        return ResponseEntity.ok(authorService.getMultiCategoryAuthors());
    }

    @Override
    @GetMapping("/with-books")
    public ResponseEntity<List<AuthorResponse>> getAuthorsWithBooks() {
        return ResponseEntity.ok(authorService.getAuthorsWithBooks());
    }

    @Override
    @GetMapping("/nationality/{nationality}")
    public ResponseEntity<List<AuthorResponse>> getAuthorsByNationality(@PathVariable String nationality) {
        return ResponseEntity.ok(authorService.getAuthorsByNationality(nationality));
    }

    @Override
    @GetMapping("/prolific")
    public ResponseEntity<List<ProlificAuthorResponse>> getProlificAuthors(@RequestParam(defaultValue = "1") int minBooks) {
        return ResponseEntity.ok(authorService.getProlificAuthors(minBooks));
    }
}
