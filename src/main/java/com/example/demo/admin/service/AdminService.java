package com.example.demo.admin.service;

import com.example.demo.news.model.Category;
import com.example.demo.news.repository.CategoryRepository;
import com.example.demo.news.model.Post;
import com.example.demo.news.repository.PostRepository;
import com.example.demo.user.model.SiteUser;
import com.example.demo.user.repository.UserRepository;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
@Service
public class AdminService {
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final PostRepository postRepository;

    // 사용자 관리
    public List<SiteUser> getUserList() {
        return userRepository.findAll();
    }

    @Transactional
    public void suspendUser(Long userId, int days) {
        SiteUser user = userRepository.findById(userId).orElseThrow();
        user.setSuspendedUntil(LocalDateTime.now().plusDays(days));
        userRepository.save(user);
    }

    @Transactional
    public void promoteToAdmin(Long userId) {
        SiteUser user = userRepository.findById(userId).orElseThrow();
        user.setRole("ROLE_ADMIN");
        userRepository.save(user);
    }

    // 분류 관리
    public List<Category> getCategoryList() {
        return categoryRepository.findByIsDeletedFalseOrderByOrdersAsc();
    }

    @Transactional
    public void addCategory(String name, String icon, int orders) {
        Category category = new Category(name);
        category.setIcon(icon);
        category.setOrders(orders);
        categoryRepository.save(category);
    }

    @Transactional
    public void editCategory(Long id, String name, String icon) {
        Category category = categoryRepository.findById(id).orElseThrow();
        category.setName(name);
        category.setIcon(icon);
        categoryRepository.save(category);
    }

    @Transactional
    public void updateCategoryOrders(List<Long> ids) {
        for (int i = 0; i < ids.size(); i++) {
            Category category = categoryRepository.findById(ids.get(i)).orElseThrow();
            category.setOrders(i + 1);
            categoryRepository.save(category);
        }
    }

    @Transactional
    public void hideCategory(Long id) {
        Category category = categoryRepository.findById(id).orElseThrow();
        category.setHidden(!category.isHidden());
        categoryRepository.save(category);
    }

    @Transactional
    public void deleteCategory(Long id) {
        Category category = categoryRepository.findById(id).orElseThrow();
        category.setDeleted(true);
        categoryRepository.save(category);
    }

    // 게시물 관리
    public Page<Post> getPostList(int page, String category, String title, String author, LocalDate date, Boolean isHidden) {
        List<Sort.Order> sorts = new ArrayList<>();
        sorts.add(Sort.Order.desc("createDate"));
        Pageable pageable = PageRequest.of(page, 10, Sort.by(sorts));

        Specification<Post> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (category != null && !category.isEmpty()) predicates.add(cb.equal(root.get("category"), category));
            if (title != null && !title.isEmpty()) predicates.add(cb.like(root.get("title"), "%" + title + "%"));
            if (author != null && !author.isEmpty()) predicates.add(cb.like(root.get("author"), "%" + author + "%"));
            if (date != null) {
                predicates.add(cb.between(root.get("createDate"), date.atStartOfDay(), date.atTime(LocalTime.MAX)));
            }
            if (isHidden != null) predicates.add(cb.equal(root.get("isHidden"), isHidden));
            return cb.and(predicates.toArray(new Predicate[0]));
        };
        return postRepository.findAll(spec, pageable);
    }

    @Transactional
    public void hidePost(Long id) {
        Post post = postRepository.findById(id).orElseThrow();
        post.setHidden(!post.isHidden());
        postRepository.save(post);
    }
}
