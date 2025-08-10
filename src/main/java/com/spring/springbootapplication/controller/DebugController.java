package com.spring.springbootapplication.controller;

import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.spring.springbootapplication.repository.UserRepository;
import com.spring.springbootapplication.entity.User;

import java.util.List;

@Profile("dev") // ★ devプロファイルのときだけ有効
@RestController
@RequestMapping("/debug")
public class DebugController {

    private final UserRepository userRepository;

    public DebugController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // ユーザー全件取得
    @GetMapping("/debug/users")
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }
}
