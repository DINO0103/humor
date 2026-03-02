package com.example.demo.news;

import com.example.demo.user.SiteUser;
import com.example.demo.user.UserService;
import com.example.demo.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.PostConstruct;
import java.security.Principal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

@RequiredArgsConstructor
@Controller
public class HomeController {

    private final PostRepository postRepository;
    private final UserService userService;
    private final CommentRepository commentRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;

    @ModelAttribute
    public void addCommonAttributes(Model model, jakarta.servlet.http.HttpServletRequest request) {
        LocalDate now = LocalDate.now();
        String formattedDate = now.format(DateTimeFormatter.ofPattern("yyyy년 M월 d일 EEEE", Locale.KOREA));
        model.addAttribute("currentDate", formattedDate);
        model.addAttribute("globalIsAdmin", false);
        
        // 숨겨지지 않고 삭제되지 않은 분류 목록만 가져와서 퀵 메뉴에 사용 (순번 정렬 추가)
        List<Category> activeCategories = categoryRepository.findByIsDeletedFalseAndIsHiddenFalseOrderByOrdersAsc();
        model.addAttribute("activeCategories", activeCategories);

        // 분류명 -> 아이콘 매핑 맵 생성 (템플릿에서 post.category로 아이콘을 바로 찾기 위함)
        java.util.Map<String, String> categoryIconMap = new java.util.HashMap<>();
        for (Category cat : activeCategories) {
            categoryIconMap.put(cat.getName(), cat.getIcon());
        }
        model.addAttribute("categoryIconMap", categoryIconMap);
    }

    @PostConstruct
    public void initData() {
        // 분류 초기화 (뉴스 홈 메뉴와 일치시킴 + 아이콘 + 순번 추가)
        if (categoryRepository.count() == 0) {
            String[][] data = {
                {"베오베", "fa-crown", "1"},
                {"베스트", "fa-star", "2"},
                {"실시간", "fa-bolt", "3"},
                {"유머", "fa-face-laugh-squint", "4"},
                {"정보", "fa-circle-info", "5"},
                {"자유", "fa-comments", "6"},
                {"더보기", "fa-ellipsis", "7"}
            };
            for (String[] item : data) {
                Category cat = new Category(item[0]);
                cat.setIcon(item[1]);
                cat.setOrders(Integer.parseInt(item[2]));
                categoryRepository.save(cat);
            }
        }

        if (userRepository.count() == 0) {
            // 초기 사용자 생성
            SiteUser admin = userService.create("admin", "admin@example.com", "admin123", "관리자");
            admin.setRole("ROLE_ADMIN");
            userRepository.save(admin);
            
            // 더미 데이터 50건 생성 (모든 분류에 골고루 분포)
            String[] allCategories = {"베오베", "베스트", "실시간", "유머", "정보", "자유", "더보기"};
            for (int i = 1; i <= 50; i++) {
                String category = allCategories[i % allCategories.length];
                int views = (int) (Math.random() * 1500);
                int recommends = (int) (Math.random() * 200);
                
                Post post = new Post(category, category + " 게시판 테스트 게시물 [" + i + "]", "상세 내용입니다. " + i, "관리자", LocalDateTime.now().minusMinutes(100 - i), views, recommends);
                post.setUser(admin);
                postRepository.save(post);
                
                // 일부 게시물에 댓글 추가
                if (i % 3 == 0) {
                    Comment comment = new Comment("테스트 댓글입니다 " + i, post, admin, null);
                    commentRepository.save(comment);
                }
            }
        }
    }

    @GetMapping("/")
    public String index(Model model, 
                        @RequestParam(value = "category", required = false) String category,
                        @RequestParam(value = "kw", defaultValue = "") String kw,
                        @RequestParam(value = "page", defaultValue = "0") int page) {
        
        // 페이징 객체 생성 (최신순 10개)
        Pageable pageable = PageRequest.of(page, 10, Sort.by(Sort.Direction.DESC, "createDate"));
        Page<Post> paging;

        if (category != null && !category.isEmpty()) {
            if (!kw.isEmpty()) {
                paging = postRepository.findAllByCategoryAndKeyword(category, kw, pageable);
            } else {
                paging = postRepository.findByCategoryAndIsHiddenFalse(category, pageable);
            }
        } else {
            if (!kw.isEmpty()) {
                paging = postRepository.findAllByKeyword(kw, pageable);
            } else {
                // 전체 목록 조회 시 숨겨진 분류 제외
                paging = postRepository.findVisiblePosts(pageable);
            }
        }
        
        // 추천수 상위 5개 게시물 조회 (숨김 분류 제외)
        List<Post> trendingPosts = postRepository.findTrendingPosts(PageRequest.of(0, 5));
        
        model.addAttribute("paging", paging);
        model.addAttribute("selectedCategory", category);
        model.addAttribute("kw", kw);
        model.addAttribute("trendingPosts", trendingPosts);
        
        return "news/index";
    }

    // 게시글 상세
    @GetMapping("/post/detail/{id}")
    public String detail(Model model, @PathVariable("id") Long id) {
        Post post = postRepository.findById(id).orElseThrow();
        // 조회수 증가
        post.setViews(post.getViews() + 1);
        postRepository.save(post);
        
        // 최신 데이터 조회를 위해 다시 조회 (댓글 포함)
        Post updatedPost = postRepository.findById(id).orElseThrow();
        model.addAttribute("post", updatedPost);
        return "news/post_detail";
    }

    // 게시글 추천
    @PostMapping("/post/recommend/{id}")
    public String recommend(@PathVariable("id") Long id) {
        Post post = postRepository.findById(id).orElseThrow();
        post.setRecommends(post.getRecommends() + 1);
        postRepository.save(post);
        return String.format("redirect:/post/detail/%s", id);
    }

    // 게시글 작성 폼
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/post/create")
    public String postCreate(Model model) {
        return "news/post_form";
    }

    // 게시글 저장
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/post/create")
    public String postCreate(@RequestParam String category, 
                             @RequestParam String title, 
                             @RequestParam String content,
                             @RequestParam(value = "files", required = false) org.springframework.web.multipart.MultipartFile[] files,
                             jakarta.servlet.http.HttpServletRequest request,
                             Principal principal) {
        Post post = new Post();
        post.setCategory(category);
        post.setTitle(title);
        post.setContent(content);
        
        SiteUser user = userService.getUser(principal.getName());
        post.setUser(user);
        post.setAuthor(user.getNickname());
        post.setCreateDate(LocalDateTime.now());
        
        // 작성자 IP 추출
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        post.setIpAddress(ip);

        // 다중 파일 업로드 처리
        if (files != null && files.length > 0) {
            String uploadDir = new java.io.File("src/main/resources/static/uploads/").getAbsolutePath();
            new java.io.File(uploadDir).mkdirs(); // 디렉토리가 없으면 생성
            
            for (org.springframework.web.multipart.MultipartFile file : files) {
                if (!file.isEmpty()) {
                    String originalName = file.getOriginalFilename();
                    String storedName = System.currentTimeMillis() + "_" + originalName;
                    try {
                        file.transferTo(new java.io.File(uploadDir + "/" + storedName));
                        PostFile postFile = new PostFile(post, originalName, storedName, "/uploads/" + storedName);
                        post.getFileList().add(postFile);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        postRepository.save(post);
        return "redirect:/";
    }

    // 댓글 및 대댓글 작성
    @Transactional
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/comment/create/{id}")
    public String commentCreate(Model model, @PathVariable("id") Long id, 
                                @RequestParam String content,
                                @RequestParam(value = "parentId", required = false) Long parentId,
                                Principal principal) {
        Post post = postRepository.findById(id).orElseThrow();
        SiteUser author = userService.getUser(principal.getName());
        
        Comment parent = null;
        if (parentId != null) {
            parent = commentRepository.findById(parentId).orElse(null);
        }

        Comment comment = new Comment(content, post, author, parent);
        commentRepository.save(comment);
        
        // 양방향 연관관계 업데이트
        post.getCommentList().add(comment);
        postRepository.save(post);
        
        return String.format("redirect:/post/detail/%s#comment_%s", id, comment.getId());
    }

    // 회원가입 및 로그인 페이지 이동 컨트롤러
    @GetMapping("/user/signup")
    public String signup() {
        return "user/signup";
    }

    @PostMapping("/user/signup")
    public String signup(@RequestParam String username, @RequestParam String email, @RequestParam String password, @RequestParam String nickname) {
        userService.create(username, email, password, nickname);
        return "redirect:/user/login";
    }

    @GetMapping("/user/login")
    public String login() {
        return "user/login";
    }
}
