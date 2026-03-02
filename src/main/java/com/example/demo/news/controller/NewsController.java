package com.example.demo.news.controller;

import com.example.demo.news.model.Category;
import com.example.demo.news.model.Post;
import com.example.demo.news.repository.CategoryRepository;
import com.example.demo.news.service.NewsService;
import com.example.demo.user.model.SiteUser;
import com.example.demo.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@RequiredArgsConstructor
@Controller
public class NewsController {

    private final NewsService newsService;
    private final UserService userService;
    private final CategoryRepository categoryRepository;

    @ModelAttribute
    public void addCommonAttributes(Model model, jakarta.servlet.http.HttpServletRequest request) {
        LocalDate now = LocalDate.now();
        String formattedDate = now.format(DateTimeFormatter.ofPattern("yyyy년 M월 d일 EEEE", Locale.KOREA));
        model.addAttribute("currentDate", formattedDate);
        model.addAttribute("globalIsAdmin", false);
        
        List<Category> activeCategories = categoryRepository.findByIsDeletedFalseAndIsHiddenFalseOrderByOrdersAsc();
        model.addAttribute("activeCategories", activeCategories);

        Map<String, String> categoryIconMap = new HashMap<>();
        for (Category cat : activeCategories) {
            categoryIconMap.put(cat.getName(), cat.getIcon());
        }
        model.addAttribute("categoryIconMap", categoryIconMap);
    }

    @GetMapping("/")
    public String index(Model model, 
                        @RequestParam(value = "category", required = false) String category,
                        @RequestParam(value = "kw", defaultValue = "") String kw,
                        @RequestParam(value = "page", defaultValue = "0") int page) {
        
        Page<Post> paging = newsService.getPostList(page, category, kw);
        List<Post> trendingPosts = newsService.getTrendingPosts();
        
        model.addAttribute("paging", paging);
        model.addAttribute("selectedCategory", category);
        model.addAttribute("kw", kw);
        model.addAttribute("trendingPosts", trendingPosts);
        
        return "news/index";
    }

    @GetMapping("/post/detail/{id}")
    public String detail(Model model, @PathVariable("id") Long id) {
        model.addAttribute("post", newsService.getPostAndIncreaseViews(id));
        return "news/post_detail";
    }

    @PostMapping("/post/recommend/{id}")
    public String recommend(@PathVariable("id") Long id) {
        newsService.recommendPost(id);
        return "redirect:/post/detail/" + id;
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/post/create")
    public String postCreate() { return "news/post_form"; }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/post/create")
    public String postCreate(@RequestParam String category, @RequestParam String title, @RequestParam String content,
                             jakarta.servlet.http.HttpServletRequest request, Principal principal) {
        // NewsService를 통한 게시글 저장 로직은 필요 시 구현
        return "redirect:/";
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/comment/create/{id}")
    public String commentCreate(@PathVariable("id") Long id, @RequestParam String content,
                                @RequestParam(value = "parentId", required = false) Long parentId, Principal principal) {
        newsService.createComment(id, content, parentId, principal.getName());
        return "redirect:/post/detail/" + id;
    }

    @GetMapping("/user/signup")
    public String signup() { return "user/signup"; }

    @PostMapping("/user/signup")
    public String signup(@RequestParam String username, @RequestParam String email, @RequestParam String password, @RequestParam String nickname) {
        userService.create(username, email, password, nickname);
        return "redirect:/user/login";
    }

    @GetMapping("/user/login")
    public String login() { return "user/login"; }
}
