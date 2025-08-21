package com.spring.springbootapplication.controller;

import com.spring.springbootapplication.service.UserService;

import org.springframework.http.HttpHeaders; 
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;


@Controller
public class HomeController {

    private final UserService userService;
    public HomeController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/home")
    public String home(Authentication auth, Model model) {
        // 認証チェック
        if (auth == null || !auth.isAuthenticated() || auth instanceof AnonymousAuthenticationToken) {
            return "redirect:/login";
        }
        
        var user = userService.findUserByEmail(auth.getName()).orElse(null);
        if (user == null) {
            return "redirect:/login";
        }

        model.addAttribute("user", user); // ← 最新のユーザーをそのまま渡す
        return "home";    // templates/home.html
        }                


        // DBに保存したアバター画像を返す
        @GetMapping("/users/{id}/avatar")
        @ResponseBody
        public ResponseEntity<byte[]> getAvatar(@PathVariable("id") Long id) {
            return userService.loadAvatar(id)
                .map(p -> {
                    HttpHeaders h = new HttpHeaders();
                    if (p.contentType() != null) h.set(HttpHeaders.CONTENT_TYPE, p.contentType());
                    // ts クエリでキャッシュ破りする前提で強めのキャッシュ
                    h.set(HttpHeaders.CACHE_CONTROL, "public, max-age=31536000, immutable");
                    return new ResponseEntity<>(p.bytes(), h, HttpStatus.OK);
                })
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
}

}
