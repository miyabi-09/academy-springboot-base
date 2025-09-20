package com.spring.springbootapplication.controller;

import com.spring.springbootapplication.dto.CategoryTotalDTO;
import com.spring.springbootapplication.service.LearningDataService;
import com.spring.springbootapplication.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;


@RestController
@RequiredArgsConstructor
public class LearningChartController {

    private final LearningDataService learningDataService;
    private final UserService userService;

    @GetMapping("/api/learning/chart")
    public List<CategoryTotalDTO> categoryChart(
        @AuthenticationPrincipal(expression = "username") String email,
        @RequestParam String month
    ) {
        Long userId = userService.findIdByEmail(email);
        return learningDataService.getCategoryTotalsByStrMonth(userId, month);
    }
}
