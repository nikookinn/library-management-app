package com.nikookinn.librarymanagement.mapper;

import com.nikookinn.librarymanagement.dto.response.BookResponse;
import com.nikookinn.librarymanagement.entity.Book;

public final class BookMapper {
    private BookMapper() {
    }

    public static BookResponse toResponse(Book book) {
        return new BookResponse(book.getId(), book.getTitle(), book.getIsbn(), book.getPublishYear(),
                book.getDescription(), book.getTotalCopies(), book.getAvailableCopies(),
                book.getCategory() != null ? book.getCategory().getId() : null,
                book.getCoverImage());
    }
}
