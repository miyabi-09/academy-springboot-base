package com.spring.springbootapplication.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class LogoutController {

    // POSTでログアウト処理
    @PostMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();  // セッションを破棄してログアウト
        return "redirect:/login";  // ログイン画面へリダイレクト
    }
}
