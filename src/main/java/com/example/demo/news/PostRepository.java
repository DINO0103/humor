package com.example.demo.news;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PostRepository extends JpaRepository<Post, Long>, JpaSpecificationExecutor<Post> {
    Page<Post> findByIsHiddenFalse(Pageable pageable);
    
    // 숨겨진 분류가 아닌 게시물만 조회 (메인 최근목록용)
    @Query("select p from Post p where p.isHidden = false and p.category not in " +
           "(select c.name from Category c where c.isHidden = true or c.isDeleted = true)")
    Page<Post> findVisiblePosts(Pageable pageable);

    Page<Post> findByCategoryAndIsHiddenFalse(String category, Pageable pageable);

    @Query("select p from Post p " +
           "where (p.isHidden = false) " +
           "  and (p.title like %:kw% or p.content like %:kw% or p.author like %:kw%)")
    Page<Post> findAllByKeyword(@Param("kw") String kw, Pageable pageable);

    @Query("select p from Post p " +
           "where (p.isHidden = false) " +
           "  and (p.category = :category) " +
           "  and (p.title like %:kw% or p.content like %:kw% or p.author like %:kw%)")
    Page<Post> findAllByCategoryAndKeyword(@Param("category") String category, @Param("kw") String kw, Pageable pageable);

    // 추천수 기준 상위 게시물 조회 (숨김 게시물 및 숨김 분류 게시물 제외)
    @Query("select p from Post p where p.isHidden = false and p.category not in " +
           "(select c.name from Category c where c.isHidden = true or c.isDeleted = true) " +
           "order by p.recommends desc")
    List<Post> findTrendingPosts(Pageable pageable);
}
