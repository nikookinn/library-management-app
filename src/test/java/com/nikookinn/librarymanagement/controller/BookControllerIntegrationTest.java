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
import com.nikookinn.librarymanagement.testsupport.AbstractIntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
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
class BookControllerIntegrationTest extends AbstractIntegrationTest {

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

        bookRepository.deleteAll();
        categoryRepository.deleteAll();
        authorRepository.deleteAll();

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
    @DisplayName("should get all books successfully")
    void shouldGetAllBooksSuccessfully() throws Exception {
        mockMvc.perform(get("/api/books"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].title").value("The Fellowship of the Ring"));
    }

    @Test
    @DisplayName("should get book by its id")
    void shouldGetBookByIdSuccessfully() throws Exception {
        mockMvc.perform(get("/api/books/{id}", book.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("The Fellowship of the Ring"));
    }

    @Test
    @DisplayName("should create a new book")
    void shouldCreateBookSuccessfully() throws Exception {
        BookCreateRequest request = new BookCreateRequest(
                "The Hobbit",
                "978-0547928227",
                1937,
                "The prequel to LOTR",
                5,
                category.getId()
        );

        mockMvc.perform(post("/api/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("The Hobbit"));
    }

    @Test
    @DisplayName("should update book information")
    void shouldUpdateBookSuccessfully() throws Exception {
        BookUpdateRequest request = new BookUpdateRequest(
                "The Fellowship of the Ring Updated",
                book.getIsbn(),
                book.getPublishYear(),
                book.getDescription(),
                15,
                category.getId()
        );

        mockMvc.perform(put("/api/books/{id}", book.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("The Fellowship of the Ring Updated"));
    }

    @Test
    @DisplayName("should delete book from system")
    void shouldDeleteBookSuccessfully() throws Exception {
        mockMvc.perform(delete("/api/books/{id}", book.getId()))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/books/{id}", book.getId()))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("should search books by title")
    void shouldSearchBooksSuccessfully() throws Exception {
        mockMvc.perform(get("/api/books/search").param("query", "fellowship"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].title").value("The Fellowship of the Ring"));
    }

    @Test
    @DisplayName("should search books with dynamic filters")
    void shouldSearchBooksDynamicSuccessfully() throws Exception {
        mockMvc.perform(get("/api/books/search/dynamic")
                        .param("title", "Fellowship")
                        .param("authorName", "Tolkien"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)));
    }

    @Test
    @DisplayName("should get all available books")
    void shouldGetAvailableBooksSuccessfully() throws Exception {
        mockMvc.perform(get("/api/books/available"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)));
    }

    @Test
    @DisplayName("should add an author to a book")
    void shouldAddAuthorToBookSuccessfully() throws Exception {
        Author newAuthor = new Author();
        newAuthor.setFirstName("New");
        newAuthor.setLastName("Author");
        newAuthor = authorRepository.save(newAuthor);

        mockMvc.perform(post("/api/books/{bookId}/authors/{authorId}", book.getId(), newAuthor.getId()))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("should get most borrowed books list")
    void shouldGetMostBorrowedBooksSuccessfully() throws Exception {
        mockMvc.perform(get("/api/books/most-borrowed"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("should get top book categories")
    void shouldGetTopCategoriesSuccessfully() throws Exception {
        mockMvc.perform(get("/api/books/top-categories"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("should get never borrowed books list")
    void shouldGetNeverBorrowedBooksSuccessfully() throws Exception {
        mockMvc.perform(get("/api/books/never-borrowed"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("should upload book cover image successfully")
    void shouldUploadBookCoverSuccessfully() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "cover.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                "test image content".getBytes()
        );

        mockMvc.perform(multipart("/api/books/{id}/cover", book.getId())
                        .file(file))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.coverImage").exists());
    }

    @Test
    @DisplayName("should download book cover image successfully")
    void shouldDownloadBookCoverSuccessfully() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "cover.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                "test image content".getBytes()
        );

        mockMvc.perform(multipart("/api/books/{id}/cover", book.getId())
                        .file(file))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/books/{id}/cover", book.getId()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.IMAGE_JPEG))
                .andExpect(content().bytes("test image content".getBytes()));
    }

    @Test
    @DisplayName("should delete book cover image successfully")
    void shouldDeleteBookCoverSuccessfully() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "cover.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                "test image content".getBytes()
        );

        mockMvc.perform(multipart("/api/books/{id}/cover", book.getId())
                        .file(file))
                .andExpect(status().isOk());

        mockMvc.perform(delete("/api/books/{id}/cover", book.getId()))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/books/{id}/cover", book.getId()))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("should update book cover image successfully")
    void shouldUpdateBookCoverSuccessfully() throws Exception {
        MockMultipartFile file1 = new MockMultipartFile(
                "file", "cover1.jpg", MediaType.IMAGE_JPEG_VALUE, "content1".getBytes());
        mockMvc.perform(multipart("/api/books/{id}/cover", book.getId()).file(file1))
                .andExpect(status().isOk());

        MockMultipartFile file2 = new MockMultipartFile(
                "file", "cover2.jpg", MediaType.IMAGE_JPEG_VALUE, "content2".getBytes());
        mockMvc.perform(multipart("/api/books/{id}/cover", book.getId()).file(file2))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.coverImage").exists());

        mockMvc.perform(get("/api/books/{id}/cover", book.getId()))
                .andExpect(status().isOk())
                .andExpect(content().bytes("content2".getBytes()));
    }

    @Test
    @DisplayName("should fail when uploading invalid file type")
    void shouldFailUploadInvalidType() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.txt",
                MediaType.TEXT_PLAIN_VALUE,
                "some text".getBytes()
        );

        mockMvc.perform(multipart("/api/books/{id}/cover", book.getId())
                        .file(file))
                .andExpect(status().isConflict());
    }
}

