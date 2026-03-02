package com.example.demo.admin.controller;

import com.example.demo.admin.service.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RequiredArgsConstructor
@Controller
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {
    private final AdminService adminService;

    @ModelAttribute
    public void addAdminAttributes(Model model) {
        model.addAttribute("globalIsAdmin", true);
        model.addAttribute("categoryList", adminService.getCategoryList());
    }

    @GetMapping("")
    public String adminMain() {
        return "redirect:/admin/users";
    }

    @GetMapping("/users")
    public String userList(Model model) {
        model.addAttribute("users", adminService.getUserList());
        return "admin/user_list";
    }

    @PostMapping("/users/suspend/{id}")
    public String suspendUser(@PathVariable("id") Long id, @RequestParam int days) {
        adminService.suspendUser(id, days);
        return "redirect:/admin/users";
    }

    @PostMapping("/users/promote/{id}")
    public String promoteUser(@PathVariable("id") Long id) {
        adminService.promoteToAdmin(id);
        return "redirect:/admin/users";
    }

    @GetMapping("/categories")
    public String categoryList(Model model) {
        model.addAttribute("categories", adminService.getCategoryList());
        return "admin/category_list";
    }

    @PostMapping("/categories/add")
    public String addCategory(@RequestParam String name, @RequestParam String icon, @RequestParam int orders) {
        adminService.addCategory(name, icon, orders);
        return "redirect:/admin/categories";
    }

    @PostMapping("/categories/edit/{id}")
    public String editCategory(@PathVariable("id") Long id, @RequestParam String name, @RequestParam String icon) {
        adminService.editCategory(id, name, icon);
        return "redirect:/admin/categories";
    }

    @PostMapping("/categories/reorder")
    @ResponseBody
    public String reorderCategories(@RequestBody List<Long> ids) {
        adminService.updateCategoryOrders(ids);
        return "success";
    }

    @PostMapping("/categories/hide/{id}")
    public String hideCategory(@PathVariable("id") Long id) {
        adminService.hideCategory(id);
        return "redirect:/admin/categories";
    }

    @PostMapping("/posts")
    public String postList(Model model,
                           @RequestParam(value = "page", defaultValue = "0") int page,
                           @RequestParam(value = "category", required = false) String category,
                           @RequestParam(value = "title", required = false) String title,
                           @RequestParam(value = "author", required = false) String author,
                           @RequestParam(value = "date", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
                           @RequestParam(value = "isHidden", required = false) Boolean isHidden) {
        model.addAttribute("paging", adminService.getPostList(page, category, title, author, date, isHidden));
        return "admin/post_list";
    }

    // 게시물 관리 탭 처리를 위한 GET 추가
    @GetMapping("/posts")
    public String postListGet(Model model,
                           @RequestParam(value = "page", defaultValue = "0") int page,
                           @RequestParam(value = "category", required = false) String category,
                           @RequestParam(value = "title", required = false) String title,
                           @RequestParam(value = "author", required = false) String author,
                           @RequestParam(value = "date", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
                           @RequestParam(value = "isHidden", required = false) Boolean isHidden) {
        model.addAttribute("paging", adminService.getPostList(page, category, title, author, date, isHidden));
        model.addAttribute("selectedCategory", category);
        model.addAttribute("selectedTitle", title);
        model.addAttribute("selectedAuthor", author);
        model.addAttribute("selectedDate", date);
        model.addAttribute("selectedIsHidden", isHidden);
        return "admin/post_list";
    }

    @PostMapping("/posts/hide/{id}")
    public String hidePost(@PathVariable("id") Long id, @RequestParam(value = "page", defaultValue = "0") int page) {
        adminService.hidePost(id);
        return "redirect:/admin/posts?page=" + page;
    }
}
