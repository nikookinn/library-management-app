package com.nikookinn.librarymanagement.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@Entity
@Table(name = "books")
@NamedEntityGraph(
    name = "Book.authorsAndCategory",
    attributeNodes = {
        @NamedAttributeNode("authors"),
        @NamedAttributeNode("category")
    }
)
public class Book {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    @NotBlank(message = "Title cannot be blank")
    private String title;

    @Column(unique = true, nullable = false)
    @NotBlank(message = "ISBN cannot be blank")
    private String isbn;

    @Column(name = "publish_year")
    private Integer publishYear;

    @Column(length = 1000)
    private String description;

    @Column(name = "total_copies", nullable = false)
    @NotNull(message = "Total copies cannot be null")
    @Positive(message = "Total copies must be positive")
    private Integer totalCopies;

    @Column(name = "available_copies", nullable = false)
    @NotNull(message = "Available copies cannot be null")
    @PositiveOrZero(message = "Available copies must be zero or positive")
    private Integer availableCopies;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "book_authors",
        joinColumns = @JoinColumn(name = "book_id"),
        inverseJoinColumns = @JoinColumn(name = "author_id")
    )
    private Set<Author> authors = new HashSet<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    @OneToMany(mappedBy = "book", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Loan> loans = new ArrayList<>();

    @Column(name = "cover_image")
    private String coverImage;

    @Version
    private Long version;

    public Book() {
    }

    public Book(Long id,
                String title,
                String isbn,
                Integer publishYear,
                String description,
                Integer totalCopies,
                Integer availableCopies,
                Set<Author> authors,
                Category category,
                List<Loan> loans) {
        this.id = id;
        this.title = title;
        this.isbn = isbn;
        this.publishYear = publishYear;
        this.description = description;
        this.totalCopies = totalCopies;
        this.availableCopies = availableCopies;
        setAuthors(authors);
        this.category = category;
        setLoans(loans);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getIsbn() {
        return isbn;
    }

    public void setIsbn(String isbn) {
        this.isbn = isbn;
    }

    public Integer getPublishYear() {
        return publishYear;
    }

    public void setPublishYear(Integer publishYear) {
        this.publishYear = publishYear;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getTotalCopies() {
        return totalCopies;
    }

    public void setTotalCopies(Integer totalCopies) {
        this.totalCopies = totalCopies;
    }

    public Integer getAvailableCopies() {
        return availableCopies;
    }

    public void setAvailableCopies(Integer availableCopies) {
        this.availableCopies = availableCopies;
    }

    public Set<Author> getAuthors() {
        return Collections.unmodifiableSet(authors);
    }

    public void setAuthors(Set<Author> authors) {
        this.authors = authors == null ? new HashSet<>() : new HashSet<>(authors);
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public List<Loan> getLoans() {
        return Collections.unmodifiableList(loans);
    }

    public void setLoans(List<Loan> loans) {
        this.loans = loans == null ? new ArrayList<>() : new ArrayList<>(loans);
    }

    /**
     * Owning side of the book-author many-to-many. Keeps both collections in sync so the
     * in-memory graph matches what is written to the {@code book_authors} join table.
     */
    public void addAuthor(Author author) {
        if (author == null || !authors.add(author)) {
            return;
        }
        author.internalGetBooks().add(this);
    }

    public void removeAuthor(Author author) {
        if (author == null || !authors.remove(author)) {
            return;
        }
        author.internalGetBooks().remove(this);
    }

    public void addLoan(Loan loan) {
        if (loan == null || loans.contains(loan)) {
            return;
        }
        loans.add(loan);
        loan.setBook(this);
    }

    public void removeLoan(Loan loan) {
        if (loan == null || !loans.remove(loan)) {
            return;
        }
        loan.setBook(null);
    }

    List<Loan> internalGetLoans() {
        return loans;
    }

    public String getCoverImage() {
        return coverImage;
    }

    public void setCoverImage(String coverImage) {
        this.coverImage = coverImage;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Book book)) return false;
        // Two unsaved instances are never equal; only a persisted identifier makes them the same book.
        return id != null && id.equals(book.id);
    }

    @Override
    public int hashCode() {
        return Book.class.hashCode();
    }

    @Override
    public String toString() {
        return "Book{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", isbn='" + isbn + '\'' +
                ", publishYear=" + publishYear +
                ", totalCopies=" + totalCopies +
                ", availableCopies=" + availableCopies +
                '}';
    }
}
