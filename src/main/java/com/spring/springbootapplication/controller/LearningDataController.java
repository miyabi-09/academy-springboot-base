package com.spring.springbootapplication.controller;

import java.util.List;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.spring.springbootapplication.dto.SkillsDTO;
import com.spring.springbootapplication.service.LearningDataService;
import com.spring.springbootapplication.service.UserService;

@Controller
public class LearningDataController {

    private final LearningDataService learningDataService;
    private final UserService userService;

    public LearningDataController(LearningDataService learningDataService, UserService userService) {
        this.learningDataService = learningDataService;
        this.userService = userService;
    }

    // （任意）古い /skills を救済
    @GetMapping("/skills")
    public String redirectSkills() {
        return "redirect:/skills-legacy";
    }

    // 一覧（従来UI）
    @GetMapping("/skills-legacy")
    public String showSkills(
            @RequestParam(value = "month", required = false) String month,
            @AuthenticationPrincipal(expression = "username") String email,
            Model model) {

        if (email == null) return "redirect:/login";

        String selectedMonth = learningDataService.normalizeYm(month);
        Long userId = userService.findIdByEmail(email);

        var skills = learningDataService.listByUserAndYm(userId, selectedMonth);
        model.addAttribute("skills", skills);

        List<LearningDataService.MonthOption> months = learningDataService.pastThreeMonths();
        model.addAttribute("months", months);
        model.addAttribute("selectedMonth", selectedMonth);
        model.addAttribute("selectedMonthLabel", learningDataService.monthLabel(selectedMonth));

        return "skills";
    }

    // 学習時間の保存（更新）
    @PostMapping("/skills/update")
    public String updateMinutes(
            @AuthenticationPrincipal(expression = "username") String email,
            @RequestParam Integer id,
            @RequestParam String month,
            @RequestParam(name = "minutes", defaultValue = "0") int minutes,
            RedirectAttributes ra) {

        Long userId = userService.findIdByEmail(email);
        int safe = Math.max(0, Math.min(minutes, 1440));
        var updated = learningDataService.updateMinutes(userId, id, safe);

        ra.addFlashAttribute("editSuccess", true);
        ra.addFlashAttribute("editedCategory", updated.getCategory().getName());
        ra.addFlashAttribute("editedName", updated.getName());
         feature/skills-chart-guard-port
        ra.addFlashAttribute("editedMinutes", safe); // テンプレ側がどちらでも読めるように

        return "redirect:/skills-legacy?month=" + learningDataService.normalizeYm(month);
    }

    // 学習時間の削除（冪等）
    @PostMapping("/skills/delete")
    public String delete(
            @AuthenticationPrincipal(expression = "username") String email,
            @RequestParam Integer id,
            @RequestParam(required = false) String month,
            RedirectAttributes ra) {

        Long userId = userService.findIdByEmail(email);

        var infoOpt = learningDataService.deleteByIdForUser(userId, id);

        // フラッシュメッセージ（存在しなくても成功扱い）
        ra.addFlashAttribute("deleteSuccess", infoOpt.isPresent());
        infoOpt.ifPresent(info -> {
            ra.addFlashAttribute("deletedCategory", info.category());
            ra.addFlashAttribute("deletedName", info.name());
        });

        return "redirect:/skills-legacy?month=" + learningDataService.normalizeYm(month);
    }

    // 新規フォーム表示
    @GetMapping("/skills/new")
    public String newSkill(
            @RequestParam String categoryName,
            @RequestParam(required = false) String month,
            Model model) {

        String normalized = learningDataService.normalizeYm(month);
        SkillsDTO form = new SkillsDTO();
        form.setMonth(normalized);

        model.addAttribute("categoryName", categoryName);
        model.addAttribute("form", form); // skills-new.html の th:object="${form}" と一致
        return "skills-new";
    }

    // 新規作成
    @PostMapping("/skills/create")
    public String create(
            @AuthenticationPrincipal(expression = "username") String email,
            @RequestParam String categoryName,
            @ModelAttribute("form") SkillsDTO form,
            RedirectAttributes ra) {

        Long userId = userService.findIdByEmail(email);
        var saved = learningDataService.saveNewByName(userId, categoryName, form);

        ra.addFlashAttribute("editSuccess", true);
        ra.addFlashAttribute("editedCategory", categoryName);
        ra.addFlashAttribute("editedName", saved.getName());
        ra.addFlashAttribute("editedMinutes", saved.getStudyTime());

        return "redirect:/skills-legacy?month=" + learningDataService.normalizeYm(form.getMonth());
    }
}
}