package com.spring.springbootapplication.controller;

import com.spring.springbootapplication.dto.UserForm;
import com.spring.springbootapplication.entity.User;
import com.spring.springbootapplication.service.UserService;

import jakarta.servlet.http.HttpSession;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class LoginController {

    private final UserService userService;

    public LoginController(UserService userService) {
        this.userService = userService;
    }

    // ログインフォーム表示
    @GetMapping("/login")
    public String showLoginForm(
            @ModelAttribute("userForm") UserForm userForm,
            @ModelAttribute("errorMessage") String errorMessage,
            Model model
    ) {
        if (!model.containsAttribute("userForm")) {
            model.addAttribute("userForm", new UserForm());
        }

        model.addAttribute("loginError", errorMessage);
        return "login";  // templates/login.html
    }

    // ログイン処理
    @PostMapping("/login")
    public String login(
            @ModelAttribute("userForm") UserForm form,
            RedirectAttributes redirectAttributes,
            HttpSession session
    ) {

// ▼ ここからログ出力
    System.out.println("=== ログイン処理開始 ===");
    System.out.println("入力されたメールアドレス: " + form.getEmail());
    System.out.println("入力されたパスワード: " + form.getPassword());
    
    User user = userService.login(form.getEmail(), form.getPassword());

    if (user != null) {
        System.out.println("ログイン成功: ユーザー名 = " + user.getName());
        session.setAttribute("loginUser", user);
        return "redirect:/top";
    } else {
        System.out.println("ログイン失敗: 該当するユーザーが見つかりません");
        redirectAttributes.addFlashAttribute("errorMessage", "メールアドレス、もしくはパスワードが間違っています");
        redirectAttributes.addFlashAttribute("userForm", form);
        return "redirect:/login";
    }
}




        //User user = userService.login(form.getEmail(), form.getPassword());

       //if (user != null) {
        //    session.setAttribute("loginUser", user);  // セッションに保存
         //   return "redirect:/top";                   // トップページに遷移
        //} else {
        //    // エラー情報をFlash Attributeに入れてリダイレクト
        //    redirectAttributes.addFlashAttribute("errorMessage", "メールアドレス、もしくはパスワードが間違っています");
        //    redirectAttributes.addFlashAttribute("userForm", form); // 入力情報を戻す
        //    return "redirect:/login";
        //}
    //}
}
