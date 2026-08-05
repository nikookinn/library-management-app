package com.nikookinn.librarymanagement.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nikookinn.librarymanagement.dto.request.BookCreateRequest;
import com.nikookinn.librarymanagement.dto.request.BookUpdateRequest;
import com.nikookinn.librarymanagement.entity.Author;
import com.nikookinn.librarymanagement.entity.Book;
import com.nikookinn.librarymanagement.entity.Category;
import com.nikookinn.librarymanagement.repository.AuthorRepository;
import com.nikookinn.librarymanagement.repository.BookRepository;
import com.nikookinn.librarymanagement.repository.CategoryRepository;
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

import java.util.Set;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@Transactional
@DisplayName("Book Controller Integration Tests")
class BookControllerIntegrationTest {

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private AuthorRepository authorRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());

    private Book book;
    private Author author;
    private Category category;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        author = new Author();
        author.setFirstName("J.R.R.");
        author.setLastName("Tolkien");
        author = authorRepository.save(author);

        category = new Category();
        category.setName("Fantasy");
        category = categoryRepository.save(category);

        book = new Book();
        book.setTitle("The Fellowship of the Ring");
        book.setIsbn("978-0618640157");
        book.setTotalCopies(10);
        book.setAvailableCopies(10);
        book.setAuthors(Set.of(author));
        book.setCategory(category);
        book = bookRepository.save(book);
    }

    @Test
    @DisplayName("should get all books")
    void shouldGetAllBooks() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/books"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].title").value("The Fellowship of the Ring"));
    }

    @Test
    @DisplayName("should get book by ID")
    void shouldGetBookById() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/books/{id}", book.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("The Fellowship of the Ring"));
    }

    @Test
    @DisplayName("should create book")
    void shouldCreateBook() throws Exception {
        // Arrange
        BookCreateRequest request = new BookCreateRequest(
                "The Hobbit",
                "978-0547928227",
                1937,
                "The prequel to LOTR",
                5,
                category.getId()
        );

        // Act & Assert
        mockMvc.perform(post("/api/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("The Hobbit"));
    }

    @Test
    @DisplayName("should update book")
    void shouldUpdateBook() throws Exception {
        // Arrange
        BookUpdateRequest request = new BookUpdateRequest(
                "The Fellowship of the Ring Updated",
                book.getIsbn(),
                book.getPublishYear(),
                book.getDescription(),
                15,
                category.getId()
        );

        // Act & Assert
        mockMvc.perform(put("/api/books/{id}", book.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("The Fellowship of the Ring Updated"));
    }

    @Test
    @DisplayName("should delete book")
    void shouldDeleteBook() throws Exception {
        // Act & Assert
        mockMvc.perform(delete("/api/books/{id}", book.getId()))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/books/{id}", book.getId()))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("should search books")
    void shouldSearchBooks() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/books/search").param("query", "fellowship"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].title").value("The Fellowship of the Ring"));
    }
}

