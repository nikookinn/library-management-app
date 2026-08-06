package com.nikookinn.librarymanagement.repository.specification;

import com.nikookinn.librarymanagement.entity.Author;
import com.nikookinn.librarymanagement.entity.Book;
import com.nikookinn.librarymanagement.entity.Category;
import com.nikookinn.librarymanagement.dto.request.BookSearchRequest;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class BookSpecification {

    public static Specification<Book> filterByRequest(BookSearchRequest request) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (request.title() != null && !request.title().isBlank()) {
                predicates.add(criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("title")),
                        "%" + request.title().toLowerCase() + "%"
                ));
            }

            if (request.isbn() != null && !request.isbn().isBlank()) {
                predicates.add(criteriaBuilder.equal(root.get("isbn"), request.isbn()));
            }

            if (request.authorName() != null && !request.authorName().isBlank()) {
                Join<Book, Author> authors = root.join("authors");
                String pattern = "%" + request.authorName().toLowerCase() + "%";
                predicates.add(criteriaBuilder.or(
                        criteriaBuilder.like(criteriaBuilder.lower(authors.get("firstName")), pattern),
                        criteriaBuilder.like(criteriaBuilder.lower(authors.get("lastName")), pattern)
                ));
            }

            if (request.categoryName() != null && !request.categoryName().isBlank()) {
                Join<Book, Category> category = root.join("category");
                predicates.add(criteriaBuilder.like(
                        criteriaBuilder.lower(category.get("name")),
                        "%" + request.categoryName().toLowerCase() + "%"
                ));
            }

            if (request.minAvailableCopies() != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(
                        root.get("availableCopies"),
                        request.minAvailableCopies()
                ));
            }

            if (request.publishYear() != null) {
                predicates.add(criteriaBuilder.equal(root.get("publishYear"), request.publishYear()));
            }

            query.distinct(true);
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
