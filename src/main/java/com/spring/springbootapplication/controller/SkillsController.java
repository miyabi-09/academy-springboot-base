package com.spring.springbootapplication.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes; // ★ 追加

import com.spring.springbootapplication.dto.SkillsDTO;
import com.spring.springbootapplication.service.LearningDataService;

import jakarta.validation.Valid;

@Controller
public class SkillsController {

    private final LearningDataService learningDataService;

    public SkillsController(LearningDataService learningDataService) {
        this.learningDataService = learningDataService;
    }

    @GetMapping("/skills")
    public String index(Model model,
                        @RequestParam(name = "month", required = false) String month) {
        Long userId = getLoginUserId();

        var months = learningDataService.pastThreeMonths();
        String selectedMonth = learningDataService.normalizeYm(month);

        model.addAttribute("months", months);                 // 月プルダウン
        model.addAttribute("selectedMonth", selectedMonth);   // "yyyy-MM"
        model.addAttribute("selectedMonthLabel", learningDataService.monthLabel(selectedMonth)); // "M月"
        model.addAttribute("skills", learningDataService.listByUserAndYm(userId, selectedMonth));
        return "skills";
    }

    // フォーム表示
    @GetMapping("/skills/new")
    public String showNew(Model model,
                          @RequestParam String categoryName,
                          @RequestParam(required = false) String month) {

        String normalizedMonth = learningDataService.normalizeYm(month);

        SkillsDTO dto = new SkillsDTO();
        dto.setMonth(normalizedMonth); // hiddenにセット

        model.addAttribute("userForm", dto);
        model.addAttribute("categoryName", categoryName);
        model.addAttribute("selectedMonth", normalizedMonth);
        return "skills-new";
    }

    // 追加保存（PRG）
    @PostMapping("/skills/new")
    public String create(@Valid @ModelAttribute("userForm") SkillsDTO form,
                        BindingResult br,
                        @RequestParam String categoryName,
                        RedirectAttributes ra,  // ★ 使う
                        Model model) {

        // 失敗時の再描画用
        model.addAttribute("categoryName", categoryName);
        model.addAttribute("selectedMonth", form.getMonth());

        // ❶ 重複チェック（カテゴリ＋月＋名前）
        String name = form.getName() == null ? "" : form.getName().trim();
        if (!name.isBlank()) {
            boolean dup = learningDataService.existsByUserMonthCategoryAndName(
                    getLoginUserId(), form.getMonth(), categoryName, name);
            if (dup) {
                br.rejectValue("name", "duplicate", name + "は既に登録されています");
            }
        }

        // ❷ BeanValidation（studyTime >= 0 など）と合わせて判定
        if (br.hasErrors()) {
            return "skills-new";
        }

        // ❸ 保存（サービス側でも二重防御）
        try {
            learningDataService.saveNewByName(getLoginUserId(), categoryName, form);
        } catch (LearningDataService.DuplicateLearningDataException ex) {
            br.rejectValue("name", "duplicate", ex.getName() + " は既に登録されています");
            return "skills-new";
        }

        // ❹ PRG: 成功情報をFlashに入れてGETへ
        ra.addFlashAttribute("addedCategory", categoryName);
        ra.addFlashAttribute("addedName", form.getName());
        ra.addFlashAttribute("addedStudyTime", form.getStudyTime());
        ra.addFlashAttribute("selectedMonth", form.getMonth());

        // 再表示時も月・カテゴリを保持
        return "redirect:/skills/new?categoryName="
                + java.net.URLEncoder.encode(categoryName, java.nio.charset.StandardCharsets.UTF_8)
                + "&month=" + form.getMonth();
    }

    private Long getLoginUserId() {
        // ログイン連携前のダミー
        return 1L;
    }
}
