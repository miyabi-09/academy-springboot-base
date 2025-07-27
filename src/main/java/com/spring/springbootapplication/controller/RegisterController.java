package com.spring.springbootapplication.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.validation.BindingResult;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.ui.Model;
import com.spring.springbootapplication.dto.UserForm;
import com.spring.springbootapplication.entity.User;

@Controller
public class RegisterController {
    @GetMapping("/register")
    public String showRegisterForm(Model model) {
        model.addAttribute("userForm", new UserForm());
        return "register"; // templates/register.html を返す
    }

    @PostMapping("/register")
public String registerUser(@Valid @ModelAttribute("userForm") UserForm userForm, BindingResult result, Model model) {
    if (result.hasErrors()) {
        return "register";
    }
    // 保存処理など
    return "login"; // 登録成功ページに遷移
}
}
