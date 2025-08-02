package com.spring.springbootapplication.service;

import org.springframework.stereotype.Service;

import com.spring.springbootapplication.entity.User;
import com.spring.springbootapplication.repository.UserRepository;
import java.util.Optional;


@Service
public class UserService {

    private final UserRepository userRepository;

    // コンストラクタ（SpringがDIしてくれる）
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }
    
    public User login(String email, String password) {
    Optional<User> optionalUser = userRepository.findByEmail(email);
    if (optionalUser.isPresent()) {
        User user = optionalUser.get();
        if (user.getPassword().equals(password)) {
            return user;
        }
    }
    return null;
    }

    // ここからsaveメソッド
    public void save(User user) {
        userRepository.save(user);
    }

    public boolean existsByEmail(String email) {
    return userRepository.existsByEmail(email);
}


} 
