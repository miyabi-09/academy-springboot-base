package com.spring.springbootapplication.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

import com.spring.springbootapplication.dto.UserForm;
import com.spring.springbootapplication.entity.User;
import com.spring.springbootapplication.service.UserService;

@Controller
public class RegisterController {

    private final UserService userService;

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
        HttpSession session
) {
    // バリデーションエラー時はフォームに戻す
    if (result.hasErrors()) {
        redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.userForm", result);
        redirectAttributes.addFlashAttribute("userForm", userForm);
        return "redirect:/register";
    }

    // 追加：メールアドレスの重複チェック
    if (userService.existsByEmail(userForm.getEmail())) {
        result.rejectValue("email", "error.userForm", "このメールアドレスは既に使われています");
        redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.userForm", result);
        redirectAttributes.addFlashAttribute("userForm", userForm);
        return "redirect:/register";
    }

    // ユーザーをエンティティにセットし保存
    User user = new User();
    user.setName(userForm.getName());
    user.setEmail(userForm.getEmail());
    user.setPassword(userForm.getPassword());
    userService.save(user);

    // 登録後、ログイン状態にしてトップ画面へ遷移
    session.setAttribute("loginUser", user);

    return "redirect:/top";
}

}
