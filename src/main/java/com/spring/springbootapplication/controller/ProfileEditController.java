package com.spring.springbootapplication.controller;

import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Map;
import java.util.UUID;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.spring.springbootapplication.service.UserService;

import org.springframework.web.bind.annotation.PostMapping;


@Controller
@RequestMapping("/profile")
public class ProfileEditController {

    private final UserService userService;

    public ProfileEditController(UserService userService){
        this.userService = userService;
    }

    //  編集画面表示
    @GetMapping("/edit")
    public String showEditForm(Authentication auth, Model model) {
        if (auth == null || !auth.isAuthenticated() || auth instanceof AnonymousAuthenticationToken) {
            return "redirect:/login";
        }
        var user = userService.findUserByEmail(auth.getName()).orElse(null);
        if (user == null) return "redirect:/login";

        model.addAttribute("user", user);
        return "profile-edit";
    } 

    @PostMapping("/edit")
    public String updateProfile(
        Authentication auth, 
        @RequestParam("introduction") String introduction,
        @RequestParam(value = "image", required = false) MultipartFile image,
        RedirectAttributes ra
    ) {
        if (auth == null || !auth.isAuthenticated() || auth instanceof AnonymousAuthenticationToken) {
            return "redirect:/login";
        }
        
        var userOpt = userService.findUserByEmail(auth.getName());
        if (userOpt.isEmpty()) {
            ra.addFlashAttribute("error","ユーザーが見つかりません");
            return "redirect:/profile/edit"; 
        }

        var user = userOpt.get();

        // バリデーション
        String v = introduction == null ? "" : introduction.trim();
        if (v.length() < 50 || v.length() > 200) {
            // フィールド専用のエラーを載せる（キー名は何でもOK）
            ra.addFlashAttribute("introError", "自己紹介は50文字以上、200文字以下で入力してください");
            ra.addFlashAttribute("form", Map.of("introduction", v));
            return "redirect:/profile/edit";
        }

        // 自己紹介を反映
        user.setIntroduction(v);

        // 画像アップロード
        if (image != null && !image.isEmpty()) {
            try (var in = image.getInputStream()) {
                Path dir = Paths.get("uploads");
                Files.createDirectories(dir);
                String filename = UUID.randomUUID() + "_" + (image.getOriginalFilename() == null ? "image" : image.getOriginalFilename());
                Path dest = dir.resolve(filename);
                Files.copy(in, dest, StandardCopyOption.REPLACE_EXISTING);
                user.setAvatarPath("/uploads/" + filename);
            } catch (Exception e) {
                System.err.println("画像保存に失敗: " + e.getMessage());
                ra.addFlashAttribute("error", "画像の保存に失敗しました");
                ra.addFlashAttribute("form", Map.of("introduction", v));
                return "redirect:/profile/edit";
            }
        }

        // 4) 保存
        userService.saveProfile(user);

        // 成功時：ホームへ戻す（/home を用意している前提）
        return "redirect:/home";


        }
    }


