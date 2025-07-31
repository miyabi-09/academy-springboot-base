package com.spring.springbootapplication.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.spring.springbootapplication.entity.User;
import java.util.Optional;


public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    //ser findByEmailAndPassword(String email, String password);
    boolean existsByEmail(String email);

}
