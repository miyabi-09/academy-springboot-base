package com.spring.springbootapplication.security;

import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;
import com.spring.springbootapplication.entity.User;
import com.spring.springbootapplication.repository.UserRepository;

@Service
public class CustomUserDetailsService implements UserDetailsService {
    private final UserRepository repo;

    public CustomUserDetailsService(UserRepository repo) {
        this.repo = repo;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        String input = username == null ? "" : username.trim();
        User u = repo.findByEmail(input)
        .orElseThrow(() -> {
            System.out.println("[AUTH FAIL] email=" + input + " (User not found)");
            return new UsernameNotFoundException("User not found: " + input);
        });

        return org.springframework.security.core.userdetails.User
                .withUsername(u.getEmail())   // ← emailをユーザー名として扱う
                .password(u.getPassword())    // ← DBのBCrypt済みハッシュをそのまま
                .authorities("ROLE_USER")
                .build();
    }
    
}

