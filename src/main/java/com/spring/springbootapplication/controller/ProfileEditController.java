package com.spring.springbootapplication.controller;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

public class ProfileEditController {
    @Controller
public class LoginController {

    //  編集画面表示
    @GetMapping("/edit")
    public String showEditForm(Authentication auth, Model model) {
        if (auth != null && auth.isAuthenticated() && !(auth instanceof AnonymousAuthenticationToken)) {
            return "redirect:/home";
        }
        model.addAttribute("isLoginPage", true);
        return "home";
    }
}
}
