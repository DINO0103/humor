package com.example.demo.news.repository;

import com.example.demo.news.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    List<Category> findByIsDeletedFalseOrderByOrdersAsc();
    List<Category> findByIsDeletedFalseAndIsHiddenFalseOrderByOrdersAsc();
}
