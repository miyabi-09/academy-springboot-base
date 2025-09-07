package com.spring.springbootapplication.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.spring.springbootapplication.entity.Category;
import com.spring.springbootapplication.repository.CategoryRepository;

@Configuration
public class BootstrapData {

  @Bean
  CommandLineRunner seedCategories(CategoryRepository categoryRepo){
    return args -> {
      if (categoryRepo.findByName("バックエンド").isEmpty()) {
        Category c = new Category(); c.setName("バックエンド"); categoryRepo.save(c);
      }
      if (categoryRepo.findByName("フロントエンド").isEmpty()) {
        Category c = new Category(); c.setName("フロントエンド"); categoryRepo.save(c);
      }
      if (categoryRepo.findByName("インフラ").isEmpty()) {
        Category c = new Category(); c.setName("インフラ"); categoryRepo.save(c);
      }
    };
  }
}

