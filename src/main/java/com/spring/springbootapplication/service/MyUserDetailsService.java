package com.spring.springbootapplication.service;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.spring.springbootapplication.entity.User;
import com.spring.springbootapplication.repository.UserRepository;

@Service
public class MyUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public MyUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        String key = (email == null) ? null : email.trim().toLowerCase();

        System.out.println("ログイン試行中: " + key); // デバッグ用ログ
    
        User user = userRepository.findByEmail(key)
            .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + key));

        // DBにはBCrypt済みパスワードが保存されていることが前提
        return org.springframework.security.core.userdetails.User
                .withUsername(user.getEmail())
                .password(user.getPassword()) // 必須（エンコード済み）
                .roles("USER") // 権限を付与（ROLE_USER）
                .build();
    }
}
