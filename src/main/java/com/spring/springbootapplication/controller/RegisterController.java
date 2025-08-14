package com.spring.springbootapplication.controller;

import com.spring.springbootapplication.dto.UserForm;
import com.spring.springbootapplication.entity.User;
import com.spring.springbootapplication.service.UserService;

import jakarta.validation.Valid;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;


@Controller
public class RegisterController {

    private final UserService userService;  
    private final DaoAuthenticationProvider dao;
    private final SecurityContextRepository scRepository = new HttpSessionSecurityContextRepository();

    public RegisterController(UserService userService, DaoAuthenticationProvider daoAuthProvider) {
        this.userService = userService; 
        this.dao = daoAuthProvider;
    }

    // 登録フォーム表示（GET）
    @GetMapping("/register")
    public String showRegisterForm(Model model) {
        if (!model.containsAttribute("userForm")) {
            model.addAttribute("userForm", new UserForm());
        }
        return "register"; // templates/register.html
    }

    // 登録処理（POST）
    @PostMapping("/register")
    public String registerUser(
        @Valid @ModelAttribute("userForm") UserForm userForm,
        BindingResult result,
        RedirectAttributes redirectAttributes,
        HttpServletRequest request, 
        HttpServletResponse response 
    ){

        // バリデーションエラー時はフォームに戻す
        if (result.hasErrors()) {
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.userForm", result);
            redirectAttributes.addFlashAttribute("userForm", userForm);
            return "redirect:/register";
        }

        // メールアドレスの重複チェック
        if (userService.existsByEmail(userForm.getEmail())) {
            result.rejectValue("email", "error.userForm", "このメールアドレスは既に使われています");
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.userForm", result);
            redirectAttributes.addFlashAttribute("userForm", userForm);
            return "redirect:/register";
        }

        // ★ 生パスワード（認証に使う）
        String rawPassword = userForm.getPassword();

        // 保存（Service側でBCryptエンコードされる前提）
        User user = new User();
        user.setName(userForm.getName());
        user.setEmail(userForm.getEmail());
        user.setPassword(userForm.getPassword()); // rawを渡すがsaveでencodeされる
        User savedUser = userService.registerUser(user);

        // ★ ここに “実ログインと同じProviderで認証” を追加
        Authentication authentication = dao.authenticate(new UsernamePasswordAuthenticationToken(savedUser.getEmail(), rawPassword)
        );

        SecurityContext context = SecurityContextHolder.createEmptyContext();
        ((UsernamePasswordAuthenticationToken) authentication)
            .setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);
        scRepository.saveContext(context, request, response);
        request.changeSessionId(); 

        // 登録完了後トップ画面へリダイレクト
        return "redirect:/home";
    }
}
