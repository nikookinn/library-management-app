package com.nikookinn.librarymanagement.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nikookinn.librarymanagement.dto.request.AuthorCreateRequest;
import com.nikookinn.librarymanagement.dto.request.AuthorUpdateRequest;
import com.nikookinn.librarymanagement.entity.Author;
import com.nikookinn.librarymanagement.repository.AuthorRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.time.LocalDate;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@Transactional
@DisplayName("Author Controller Integration Tests")
class AuthorControllerIntegrationTest {

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private AuthorRepository authorRepository;

    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());

    private Author author;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        author = new Author();
        author.setFirstName("J.R.R.");
        author.setLastName("Tolkien");
        author.setBirthDate(LocalDate.of(1892, 1, 3));
        author.setNationality("British");
        author.setBiography("Author of The Lord of the Rings");
        author = authorRepository.save(author);
    }

    @Test
    @DisplayName("should get all authors successfully")
    void shouldGetAllAuthorsSuccessfully() throws Exception {
        mockMvc.perform(get("/api/authors"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].firstName").value("J.R.R."));
    }

    @Test
    @DisplayName("should get author by their id")
    void shouldGetAuthorByIdSuccessfully() throws Exception {
        mockMvc.perform(get("/api/authors/{id}", author.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("J.R.R."));
    }

    @Test
    @DisplayName("should create a new author")
    void shouldCreateAuthorSuccessfully() throws Exception {
        AuthorCreateRequest request = new AuthorCreateRequest(
                "J.K.",
                "Rowling",
                LocalDate.of(1965, 7, 31),
                "British",
                "Author of Harry Potter"
        );

        mockMvc.perform(post("/api/authors")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.firstName").value("J.K."))
                .andExpect(jsonPath("$.lastName").value("Rowling"));
    }

    @Test
    @DisplayName("should update author information")
    void shouldUpdateAuthorSuccessfully() throws Exception {
        AuthorUpdateRequest request = new AuthorUpdateRequest(
                "J.R.R. Updated",
                "Tolkien Updated",
                author.getBirthDate(),
                author.getNationality(),
                author.getBiography()
        );

        mockMvc.perform(put("/api/authors/{id}", author.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("J.R.R. Updated"));
    }

    @Test
    @DisplayName("should delete author from system")
    void shouldDeleteAuthorSuccessfully() throws Exception {
        mockMvc.perform(delete("/api/authors/{id}", author.getId()))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/authors/{id}", author.getId()))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("should search authors by name")
    void shouldSearchAuthorsSuccessfully() throws Exception {
        mockMvc.perform(get("/api/authors/search").param("name", "tolkien"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].lastName").value("Tolkien"));
    }
}
