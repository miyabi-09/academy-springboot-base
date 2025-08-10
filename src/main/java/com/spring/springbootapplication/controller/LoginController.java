package com.spring.springbootapplication.controller;

import com.spring.springbootapplication.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class LoginController {

    @Autowired
    private UserService userService;

    // ログイン画面の表示
    @GetMapping("/login")
    public String showLoginForm(Authentication auth, Model model) {
        if (auth != null && auth.isAuthenticated() && !(auth instanceof AnonymousAuthenticationToken)) {
            return "redirect:/home";
        }
        model.addAttribute("isLoginPage", true);
        return "login";
    }
}