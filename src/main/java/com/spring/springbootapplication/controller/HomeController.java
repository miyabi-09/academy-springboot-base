package com.spring.springbootapplication.controller;

import com.spring.springbootapplication.service.UserService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;


@Controller
public class HomeController {

    private final UserService userService;
    public HomeController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/home")
    public String home(Authentication auth, Model model) {
        // 認証チェック
        if (auth == null || !auth.isAuthenticated())
            return "redirect:/login";

        var user = userService.findUserByEmail(auth.getName()).orElse(null);
        if (user == null) {
            return "redirect:/login";
        }

        model.addAttribute("user", user); // ← 最新のユーザーをそのまま渡す
        return "home";    // templates/home.html
        }                
}
