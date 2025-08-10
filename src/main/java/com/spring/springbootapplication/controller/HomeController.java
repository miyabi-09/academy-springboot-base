package com.spring.springbootapplication.controller;

import com.spring.springbootapplication.entity.User;
import com.spring.springbootapplication.service.UserService;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
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
        if (auth == null || !auth.isAuthenticated()
                || "anonymousUser".equals(String.valueOf(auth.getPrincipal()))) {
            return "redirect:/login";
        }

        // ★ここで毎回、email を取り出す（メソッド外に出さない）
        String email;
        Object principal = auth.getPrincipal();
        if (principal instanceof org.springframework.security.core.userdetails.User u) {
            email = u.getUsername();
        } else if (principal instanceof UserDetails ud) {
            email = ud.getUsername();
        } else {
            email = auth.getName();
        }

        // DBからユーザー取得（存在しない時のNPE回避）
        User loginUser = userService.findByEmail(email).orElse(null);

        model.addAttribute("userName", loginUser != null ? loginUser.getName() : email);
        model.addAttribute("bio", "自己紹介がまだ登録されていません。");
        model.addAttribute("image", "");

        return "home";
    }
}
