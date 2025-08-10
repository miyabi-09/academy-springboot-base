package com.spring.springbootapplication.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import com.spring.springbootapplication.dto.UserForm;
import com.spring.springbootapplication.entity.User;
import com.spring.springbootapplication.service.UserService;

@Controller
public class RegisterController {

    private final UserService    userService;  
    private final SecurityContextRepository scRepository
        = new HttpSessionSecurityContextRepository();

    @Autowired
    public RegisterController(UserService userService) { 
        this.userService = userService; 
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
        HttpSession session,
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
        //user.setPassword(rawPassword);
        user.setPassword(userForm.getPassword()); // rawを渡すがsaveでencodeされる
        User savedUser = userService.save(user);
        
        //    代わりに UserDetails とトークンを自前で作成
        UserDetails userDetails = org.springframework.security.core.userdetails.User
            .withUsername(savedUser.getEmail())
            .password(savedUser.getPassword())
            .authorities("ROLE_USER")
            .build();

        UsernamePasswordAuthenticationToken authToken =
            new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authToken);
        SecurityContextHolder.setContext(context);
        scRepository.saveContext(context, request, response);

        // 登録完了後トップ画面へリダイレクト
        return "redirect:/home";
    }
}
