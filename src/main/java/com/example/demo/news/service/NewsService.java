package com.example.demo.news.service;

import com.example.demo.news.model.Category;
import com.example.demo.news.model.Comment;
import com.example.demo.news.model.Post;
import com.example.demo.news.repository.CategoryRepository;
import com.example.demo.news.repository.CommentRepository;
import com.example.demo.news.repository.PostRepository;
import com.example.demo.user.model.SiteUser;
import com.example.demo.user.repository.UserRepository;
import com.example.demo.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@RequiredArgsConstructor
@Service
public class NewsService {

    private final PostRepository postRepository;
    private final CategoryRepository categoryRepository;
    private final CommentRepository commentRepository;
    private final UserRepository userRepository;
    private final UserService userService;

    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void initData() {
        System.out.println("========== NEWS SERVICE DATA INITIALIZATION START ==========");
        // 1. 분류 초기화
        if (categoryRepository.count() == 0) {
            String[][] data = {
                {"베오베", "fa-crown", "1"}, {"베스트", "fa-star", "2"}, {"실시간", "fa-bolt", "3"},
                {"유머", "fa-face-laugh-squint", "4"}, {"정보", "fa-circle-info", "5"},
                {"자유", "fa-comments", "6"}, {"더보기", "fa-ellipsis", "7"}
            };
            for (String[] item : data) {
                Category cat = new Category(item[0]);
                cat.setIcon(item[1]);
                cat.setOrders(Integer.parseInt(item[2]));
                categoryRepository.save(cat);
            }
        }

        // 2. 초기 관리자 및 더미 게시물 생성
        if (userRepository.count() == 0) {
            SiteUser admin = userService.create("admin", "admin@example.com", "admin123", "관리자");
            admin.setRole("ROLE_ADMIN");
            userRepository.save(admin);
            
            String[] allCategories = {"베오베", "베스트", "실시간", "유머", "정보", "자유", "더보기"};
            for (int i = 1; i <= 50; i++) {
                String category = allCategories[i % allCategories.length];
                Post post = new Post(category, category + " 게시판 테스트 게시물 [" + i + "]", 
                                   "상세 내용입니다. " + i, "관리자", 
                                   LocalDateTime.now().minusMinutes(100 - i), 
                                   (int)(Math.random()*1500), (int)(Math.random()*200));
                post.setUser(admin);
                postRepository.save(post);
                
                if (i % 3 == 0) {
                    commentRepository.save(new Comment("테스트 댓글 " + i, post, admin, null));
                }
            }
        }
    }

    // 메인 페이지 게시물 조회 로직
    public Page<Post> getPostList(int page, String category, String kw) {
        Pageable pageable = PageRequest.of(page, 10, Sort.by(Sort.Direction.DESC, "createDate"));
        if (category != null && !category.isEmpty()) {
            return kw.isEmpty() ? postRepository.findByCategoryAndIsHiddenFalse(category, pageable) 
                               : postRepository.findAllByCategoryAndKeyword(category, kw, pageable);
        } else {
            return kw.isEmpty() ? postRepository.findVisiblePosts(pageable) 
                               : postRepository.findAllByKeyword(kw, pageable);
        }
    }

    public List<Post> getTrendingPosts() {
        return postRepository.findTrendingPosts(PageRequest.of(0, 5));
    }

    @Transactional
    public Post getPostAndIncreaseViews(Long id) {
        Post post = postRepository.findById(id).orElseThrow();
        post.setViews(post.getViews() + 1);
        return post;
    }

    @Transactional
    public void recommendPost(Long id) {
        Post post = postRepository.findById(id).orElseThrow();
        post.setRecommends(post.getRecommends() + 1);
    }

    @Transactional
    public void createComment(Long postId, String content, Long parentId, String username) {
        Post post = postRepository.findById(postId).orElseThrow();
        SiteUser author = userService.getUser(username);
        Comment parent = (parentId != null) ? commentRepository.findById(parentId).orElse(null) : null;
        commentRepository.save(new Comment(content, post, author, parent));
    }
}
