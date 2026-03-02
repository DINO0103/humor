package com.example.demo.game.controller;

import com.example.demo.news.model.Category;
import com.example.demo.news.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

@RequiredArgsConstructor
@Controller
public class GameController {

    private final CategoryRepository categoryRepository;

    @ModelAttribute
    public void addCommonAttributes(Model model, jakarta.servlet.http.HttpServletRequest request) {
        LocalDate now = LocalDate.now();
        String formattedDate = now.format(DateTimeFormatter.ofPattern("yyyy년 M월 d일 EEEE", Locale.KOREA));
        model.addAttribute("currentDate", formattedDate);
        model.addAttribute("globalIsAdmin", false);
        
        List<Category> activeCategories = categoryRepository.findByIsDeletedFalseAndIsHiddenFalseOrderByOrdersAsc();
        model.addAttribute("activeCategories", activeCategories);
    }

    @GetMapping("/game")
    public String gameMain() {
        return "game/game_main";
    }

    @GetMapping("/tetris")
    public String tetris() {
        return "game/tetris";
    }

    @GetMapping("/snake")
    public String snake() {
        return "game/snake";
    }

    @GetMapping("/minesweeper")
    public String minesweeper() {
        return "game/minesweeper";
    }

    @GetMapping("/ranking")
    public String ranking() {
        return "game/ranking";
    }
}
