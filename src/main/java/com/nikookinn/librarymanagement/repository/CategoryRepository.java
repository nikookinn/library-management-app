package com.nikookinn.librarymanagement.repository;

import com.nikookinn.librarymanagement.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Category, Long> {
}
