package com.spring.springbootapplication.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.spring.springbootapplication.entity.User;
import com.spring.springbootapplication.repository.UserRepository;

import java.util.Optional;

@Service
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public User login(String email, String password) {
        Optional<User> optionalUser = userRepository.findByEmail(email);
        if (optionalUser.isPresent()) {
            User user = optionalUser.get();
            // パスワードをエンコード済みと比較
            if (passwordEncoder.matches(password, user.getPassword())) {
                return user;
            }
        }
        return null;
    }

    private String normalizeEmail(String email) {
        return email == null ? null : email.trim().toLowerCase();
    }

    
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(normalizeEmail(email));
        //String key = email == null ? "" : email.trim();
        //return userRepository.existsByEmail(key);
    }

    public Optional<User> findUserByEmail(String email) {
        return userRepository.findByEmail(normalizeEmail(email));
        //return userRepository.findByEmail(email == null ? null : email.trim());
    }

    public User registerUser(User user) {
        user.setEmail(normalizeEmail(user.getEmail()));
        // パスワードをハッシュ化してから保存
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepository.save(user);
    }

    @Transactional
    public void saveProfile(User user) {
        // パスワードの再エンコードなどは一切しないで保存
        userRepository.save(user);
    }

}
